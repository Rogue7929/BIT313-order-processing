package com.project.reactiveorders.repository;

import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory inventory repository
 * Uses ConcurrentHashMap for thread-safe inventory management
 * 
 * Simulates an inventory database with preloaded product stock
 */
@Repository
public class InventoryRepository {
    private final ConcurrentHashMap<String, Integer> inventory;

    public InventoryRepository() {
        this.inventory = new ConcurrentHashMap<>();
        // Initialize inventory with demo products
        inventory.put("Laptop", 500);
        inventory.put("Mouse", 2000);
        inventory.put("Keyboard", 1000);
        inventory.put("Monitor", 300);
        inventory.put("Headphones", 800);
        inventory.put("Webcam", 600);
    }

    /**
     * Get current stock for a product
     * @param productId the product identifier
     * @return current stock quantity, or 0 if product not found
     */
    public Integer getStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    /**
     * Check if sufficient stock is available
     * @param productId the product identifier
     * @param quantity requested quantity
     * @return true if stock is available, false otherwise
     */
    public Boolean hasStock(String productId, Integer quantity) {
        Integer currentStock = inventory.getOrDefault(productId, 0);
        return currentStock >= quantity;
    }

    /**
     * Reserve (deduct) stock from inventory
     * Thread-safe operation using ConcurrentHashMap's atomic operations
     * 
     * @param productId the product identifier
     * @param quantity quantity to reserve
     * @return true if reservation successful, false if insufficient stock
     */
    public Boolean reserveStock(String productId, Integer quantity) {
        return inventory.computeIfPresent(productId, (key, currentStock) -> {
            if (currentStock >= quantity) {
                return currentStock - quantity;
            }
            return currentStock;  // No change if insufficient stock
        }) != null && hasStock(productId, 0);
    }

    /**
     * Atomic operation to reserve stock with validation
     * Ensures thread-safety during reservation
     * 
     * @param productId the product identifier
     * @param quantity quantity to reserve
     * @return true if reservation successful
     */
    public Boolean tryReserveStock(String productId, Integer quantity) {
        while (true) {
            Integer currentStock = inventory.get(productId);
            if (currentStock == null || currentStock < quantity) {
                return false;
            }
            if (inventory.replace(productId, currentStock, currentStock - quantity)) {
                return true;
            }
        }
    }

    /**
     * Release (return) stock to inventory
     * Used for rollback operations when payment fails
     * 
     * @param productId the product identifier
     * @param quantity quantity to release
     */
    public void releaseStock(String productId, Integer quantity) {
        inventory.computeIfPresent(productId, (key, currentStock) -> currentStock + quantity);
    }

    /**
     * Get all inventory as a snapshot
     * @return copy of current inventory map
     */
    public Map<String, Integer> getInventorySnapshot() {
        return new ConcurrentHashMap<>(inventory);
    }
}
