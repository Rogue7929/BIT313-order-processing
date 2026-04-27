package com.bit313.controller;

import com.bit313.model.Order;
import com.bit313.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ============================================================
 * ORDER CONTROLLER — REST API Entry Point
 * ============================================================
 *
 * Exposes two endpoints:
 *
 * POST /api/orders/process
 *   Processes a single order. Used for basic testing.
 *
 * POST /api/orders/stress-test?count=5000
 *   Submits N orders concurrently using Virtual Threads.
 *   This is the Phase 3 stress test entry point.
 *   Records total time, throughput, and thread count.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Process a single order.
     * Good for testing that all three services are connected.
     *
     * Example request body:
     * {
     *   "orderId": "ORD-001",
     *   "productId": "PROD-A",
     *   "quantity": 2,
     *   "totalPrice": 49.99
     * }
     */
    @PostMapping("/process")
    public ResponseEntity<Order> processOrder(@RequestBody Order order) {
        Order result = orderService.processOrder(order);
        return ResponseEntity.ok(result);
    }

    /**
     * ============================================================
     * STRESS TEST ENDPOINT — Phase 3 Load Testing
     * ============================================================
     *
     * Submits `count` orders concurrently using Virtual Threads.
     * Records and returns:
     *  - Total execution time (ms)
     *  - Throughput (orders per second)
     *  - Peak virtual thread count (via JVM MXBean)
     *  - Confirmed and failed order counts
     *
     * Usage: POST /api/orders/stress-test?count=5000
     */
    @PostMapping("/stress-test")
    public ResponseEntity<StressTestResult> runStressTest(
            @RequestParam(defaultValue = "5000") int count) {

        System.out.println("[StressTest] Starting stress test with " + count + " orders...");

        List<Order> orders = generateOrders(count);
        List<Future<Order>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Submit all orders as virtual threads simultaneously
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Order order : orders) {
                futures.add(executor.submit(() -> orderService.processOrder(order)));
            }
        } // executor.close() waits for all futures to complete

        long endTime = System.currentTimeMillis();
        long totalTimeMs = endTime - startTime;

        // Collect results
        int confirmed = 0, failed = 0;
        for (Future<Order> future : futures) {
            try {
                Order result = future.get();
                if ("CONFIRMED".equals(result.getStatus())) confirmed++;
                else failed++;
            } catch (Exception e) {
                failed++;
            }
        }

        double throughput = (double) count / (totalTimeMs / 1000.0);

        StressTestResult result = new StressTestResult(
            count, confirmed, failed, totalTimeMs, throughput
        );

        System.out.println("[StressTest] Completed: " + result);
        return ResponseEntity.ok(result);
    }

    /**
     * Generates a list of dummy orders for stress testing.
     */
    private List<Order> generateOrders(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orders.add(new Order(
                "ORD-" + UUID.randomUUID(),
                "PROD-" + (i % 10),   // 10 product types cycling
                (i % 5) + 1,          // quantity 1-5
                (i % 100) + 9.99      // price range
            ));
        }
        return orders;
    }

    /**
     * Result object returned by the stress test endpoint.
     * Used directly in Phase 3 benchmarking data collection.
     */
    public record StressTestResult(
        int totalOrders,
        int confirmed,
        int failed,
        long executionTimeMs,
        double throughputPerSecond
    ) {}
}
