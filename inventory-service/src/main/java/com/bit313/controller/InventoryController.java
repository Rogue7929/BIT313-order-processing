package com.bit313.controller;

import com.bit313.model.Order;
import com.bit313.model.ServiceResponse;
import com.bit313.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * INVENTORY CONTROLLER — REST API
 * ============================================================
 *
 * Exposes: POST /api/inventory/check
 *
 * Called by Order Service to verify product availability
 * before processing payment.
 *
 * With Virtual Threads enabled in Spring Boot (via
 * VirtualThreadConfig), each request to this endpoint
 * automatically runs on its own virtual thread.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Check inventory availability for an order.
     *
     * POST /api/inventory/check
     * Body: Order JSON
     * Returns: ServiceResponse (success true/false + message)
     */
    @PostMapping("/check")
    public ResponseEntity<ServiceResponse> checkInventory(@RequestBody Order order) {
        ServiceResponse response = inventoryService.checkInventory(order);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     * Useful to verify the service is running before stress tests.
     *
     * GET /api/inventory/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is running | Thread: " +
                                 Thread.currentThread());
    }
}
