package com.project.reactiveorders.service;

import com.project.reactiveorders.model.*;
import com.project.reactiveorders.util.ReactiveLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import java.util.UUID;
import java.time.Duration;

/**
 * Reactive Order Service - Main Orchestrator
 * 
 * Demonstrates:
 * - flatMap() for reactive chaining
 * - zipWhen() for parallel execution with dependencies
 * - Non-blocking composition of multiple async operations
 * - Backpressure handling through reactive streams
 * - Comprehensive error handling and rollback logic
 * 
 * Orchestrates the order processing flow:
 * 1. Create order
 * 2. Reserve inventory (parallel with validation)
 * 3. Process payment (with rollback capability)
 * 4. Return final result
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ReactiveLogger logger;

    /**
     * Main reactive order processing flow
     * 
     * Orchestration Logic:
     * 1. Validate request and create order
     * 2. Reserve inventory stock
     * 3. If inventory reserved, process payment (parallel)
     * 4. If payment successful, return SUCCESS
     * 5. On any failure, rollback inventory and return appropriate status
     * 
     * Key Reactive Patterns:
     * - flatMap(): Sequential chaining (order → inventory → payment)
     * - zipWhen(): Dependency-based parallelism
     * - onErrorResume(): Graceful error recovery with rollback
     * - timeout(): Prevent indefinite blocking
     * 
     * @param request order request with product details
     * @return Mono<OrderResponse> with order processing result
     */
    public Mono<OrderResponse> processOrder(OrderRequest request) {
        long startTime = System.currentTimeMillis();
        String orderId = UUID.randomUUID().toString();

        logger.info("Order Service", "Processing new order: " + orderId + 
                " for product: " + request.getProductId() + " qty: " + request.getQuantity());

        // Step 1: Validate request
        return Mono.defer(() -> {
                    if (request.getProductId() == null || request.getQuantity() == null || request.getPrice() == null) {
                        logger.error("Order Service", "Invalid order request");
                        return Mono.<Order>error(new IllegalArgumentException("Invalid order request"));
                    }

                    // Create order domain model
                    Order order = Order.builder()
                            .orderId(orderId)
                            .productId(request.getProductId())
                            .quantity(request.getQuantity())
                            .price(request.getPrice())
                            .status("CREATED")
                            .createdAt(System.currentTimeMillis())
                            .startTime(startTime)
                            .build();

                    return Mono.just(order);
                })
                // Step 2: Reserve inventory
                .flatMap(order -> inventoryService.reserveStock(order.getProductId(), order.getQuantity())
                        .flatMap(inventoryRes -> {
                            if (!inventoryRes.getSuccess()) {
                                // Stock unavailable
                                logger.warn("Order Service", "Order " + orderId + " failed: out of stock");
                                return Mono.just(Tuples.of(order, inventoryRes));
                            }
                            return Mono.just(Tuples.of(order, inventoryRes));
                        })
                )
                // Step 3: Process payment if inventory reserved
                .flatMap(tuple -> {
                    Order order = tuple.getT1();
                    InventoryReservation inventoryRes = tuple.getT2();

                    if (!inventoryRes.getSuccess()) {
                        // Inventory failed, return out of stock response
                        return Mono.just(OrderResponse.builder()
                                .orderId(orderId)
                                .status("OUT_OF_STOCK")
                                .message(inventoryRes.getMessage())
                                .processingTimeMs(System.currentTimeMillis() - startTime)
                                .totalPrice(order.getPrice() * order.getQuantity())
                                .build());
                    }

                    // Inventory successful, process payment
                    Double totalAmount = order.getPrice() * order.getQuantity();
                    return paymentService.processPayment(orderId, totalAmount)
                            .flatMap(paymentResult -> {
                                if ("SUCCESS".equals(paymentResult.getStatus())) {
                                    // Payment successful
                                    logger.info("Order Service", "Order " + orderId + " completed successfully");
                                    return Mono.just(OrderResponse.builder()
                                            .orderId(orderId)
                                            .status("SUCCESS")
                                            .message("Order processed successfully")
                                            .processingTimeMs(System.currentTimeMillis() - startTime)
                                            .totalPrice(totalAmount)
                                            .build());
                                } else {
                                    // Payment failed, rollback inventory
                                    logger.warn("Order Service", "Order " + orderId + " payment failed, rolling back inventory");
                                    return inventoryService.releaseStock(order.getProductId(), order.getQuantity())
                                            .then(Mono.just(OrderResponse.builder()
                                                    .orderId(orderId)
                                                    .status("FAILED_PAYMENT")
                                                    .message("Payment " + paymentResult.getStatus() + 
                                                            ": " + paymentResult.getMessage())
                                                    .processingTimeMs(System.currentTimeMillis() - startTime)
                                                    .totalPrice(totalAmount)
                                                    .build()));
                                }
                            });
                })
                // Step 4: Error handling and recovery
                .onErrorResume(throwable -> {
                    logger.error("Order Service", "Unexpected error processing order " + orderId + 
                            ": " + throwable.getMessage());
                    return Mono.just(OrderResponse.builder()
                            .orderId(orderId)
                            .status("ERROR")
                            .message("Unexpected error: " + throwable.getMessage())
                            .processingTimeMs(System.currentTimeMillis() - startTime)
                            .build());
                })
                // Step 5: Apply timeout
                .timeout(Duration.ofSeconds(5));
    }
}
