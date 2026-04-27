package com.bit313.service;

import com.bit313.model.Order;
import com.bit313.model.ServiceResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================
 * INVENTORY SERVICE — Business Logic
 * ============================================================
 *
 * Simulates a real inventory database check.
 *
 * In a production system this would query a database.
 * Here we use an in-memory ConcurrentHashMap to simulate stock
 * levels safely across concurrent Virtual Thread requests.
 *
 * ConcurrentHashMap is used (not HashMap) because multiple
 * virtual threads may access stock data simultaneously.
 *
 * Simulated network latency:
 * Thread.sleep(50) simulates a database query taking 50ms.
 * With Virtual Threads, this sleep does NOT waste an OS thread.
 */
@Service
public class InventoryService {

    // Simulated stock levels for 10 products
    // ConcurrentHashMap is thread-safe — safe for virtual threads
    private final Map<String, Integer> stockLevels = new ConcurrentHashMap<>(Map.of(
        "PROD-0", 1000,
        "PROD-1", 500,
        "PROD-2", 750,
        "PROD-3", 300,
        "PROD-4", 1200,
        "PROD-5", 800,
        "PROD-6", 450,
        "PROD-7", 600,
        "PROD-8", 900,
        "PROD-9", 250
    ));

    /**
     * Checks whether the requested quantity of a product is available.
     * Simulates a 50ms database query using Thread.sleep().
     *
     * @param order The order containing productId and quantity
     * @return ServiceResponse indicating stock availability
     */
    public ServiceResponse checkInventory(Order order) {

        System.out.println("[InventoryService] Checking stock for product: " +
                           order.getProductId() + " | Thread: " + Thread.currentThread());

        // Simulate database query latency (50ms)
        // Virtual thread parks here — carrier thread is freed
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ServiceResponse(false, "Inventory check interrupted");
        }

        String productId = order.getProductId();
        int requested    = order.getQuantity();

        // Check if product exists in our inventory
        if (!stockLevels.containsKey(productId)) {
            return new ServiceResponse(false, "Product not found: " + productId);
        }

        int available = stockLevels.get(productId);

        // Check if enough stock is available
        if (available < requested) {
            return new ServiceResponse(false,
                "Insufficient stock for " + productId +
                ". Available: " + available + ", Requested: " + requested);
        }

        // Reserve the stock (atomic update for thread safety)
        stockLevels.merge(productId, -requested, Integer::sum);

        System.out.println("[InventoryService] Stock reserved for " + productId +
                           ". Remaining: " + stockLevels.get(productId));

        return new ServiceResponse(true, "Stock confirmed for " + productId);
    }
}
