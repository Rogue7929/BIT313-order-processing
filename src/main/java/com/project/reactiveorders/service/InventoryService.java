package com.project.reactiveorders.service;

import com.project.reactiveorders.exception.OutOfStockException;
import com.project.reactiveorders.model.InventoryReservation;
import com.project.reactiveorders.repository.InventoryRepository;
import com.project.reactiveorders.util.ReactiveLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;

/**
 * Reactive Inventory Service
 * 
 * Demonstrates:
 * - Non-blocking I/O with Mono
 * - Simulated network latency with delayElement()
 * - subscribeOn() for thread pool execution
 * - Backpressure handling through reactive operators
 * 
 * Responsibilites:
 * - Check product availability
 * - Reserve stock atomically
 * - Handle inventory timeouts and failures
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ReactiveLogger logger;

    /**
     * Reactively reserve stock with timeout and latency simulation
     * 
     * Reactive Flow:
     * 1. Validate input parameters
     * 2. Simulate network latency (50-150ms)
     * 3. Check stock availability
     * 4. Perform atomic reservation
     * 5. Apply timeout of 2 seconds
     * 
     * @param productId product identifier
     * @param quantity quantity to reserve
     * @return Mono<InventoryReservation> with reservation result
     */
    public Mono<InventoryReservation> reserveStock(String productId, Integer quantity) {
        return Mono.defer(() -> {
                    logger.info("Inventory Service", "Checking stock for " + productId + " qty: " + quantity);

                    // Simulate network latency: 50-150ms
                    long latency = 50 + (long)(Math.random() * 100);

                    return Mono.fromCallable(() -> {
                                Integer currentStock = inventoryRepository.getStock(productId);

                                // Validate sufficient stock
                                if (currentStock < quantity) {
                                    logger.warn("Inventory Service", "Insufficient stock for " + productId + 
                                            ". Available: " + currentStock + ", Requested: " + quantity);
                                    throw new OutOfStockException("Insufficient stock for " + productId);
                                }

                                // Perform atomic reservation
                                if (!inventoryRepository.tryReserveStock(productId, quantity)) {
                                    logger.warn("Inventory Service", "Failed to reserve stock for " + productId);
                                    throw new OutOfStockException("Failed to reserve stock");
                                }

                                logger.info("Inventory Service", "Stock reserved for " + productId + 
                                        ". Remaining: " + (currentStock - quantity));

                                return InventoryReservation.builder()
                                        .productId(productId)
                                        .reservedQuantity(quantity)
                                        .success(true)
                                        .message("Stock reserved successfully")
                                        .build();
                            })
                            .delayElement(Duration.ofMillis(latency))  // Simulate network latency
                            .subscribeOn(Schedulers.boundedElastic());  // Execute on elastic thread pool
                })
                .timeout(Duration.ofSeconds(2))  // 2-second timeout for inventory check
                .onErrorResume(throwable -> {
                    logger.error("Inventory Service", "Error reserving stock: " + throwable.getMessage());
                    return Mono.just(InventoryReservation.builder()
                            .productId(productId)
                            .success(false)
                            .message(throwable.getMessage())
                            .build());
                });
    }

    /**
     * Reactively release reserved stock (rollback operation)
     * Used when payment fails after inventory reservation
     * 
     * @param productId product identifier
     * @param quantity quantity to release
     * @return Mono<Void>
     */
    public Mono<Void> releaseStock(String productId, Integer quantity) {
        return Mono.fromRunnable(() -> {
                    logger.info("Inventory Service", "Releasing stock for " + productId + " qty: " + quantity);
                    inventoryRepository.releaseStock(productId, quantity);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * Get current inventory snapshot (for monitoring)
     * @return Mono with inventory map
     */
    public Mono<java.util.Map<String, Integer>> getInventorySnapshot() {
        return Mono.fromCallable(inventoryRepository::getInventorySnapshot)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
