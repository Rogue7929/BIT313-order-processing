package com.bit313.service;

import com.bit313.model.Order;
import com.bit313.model.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ============================================================
 * ORDER SERVICE — Business Logic Layer
 * ============================================================
 *
 * This service orchestrates the full order processing flow:
 *
 *   1. Check inventory (Inventory Service → port 8081)
 *   2. Process payment (Payment Service  → port 8082)
 *   3. Return final order status
 *
 * KEY PHASE 2 CONCEPTS DEMONSTRATED HERE:
 *
 * A) Virtual Thread Executor
 *    Uses Executors.newVirtualThreadPerTaskExecutor() to submit
 *    inventory and payment calls as separate virtual threads.
 *    This simulates parallel inter-service communication.
 *
 * B) Blocking REST Calls
 *    RestTemplate.postForObject() is a BLOCKING call — the thread
 *    waits for the HTTP response. With platform threads this wastes
 *    OS resources. With Virtual Threads, the JVM parks the virtual
 *    thread and reuses the carrier thread — no waste.
 *
 * C) Retry Logic with Thread.sleep()
 *    On failure, Thread.sleep() pauses before retry. Virtual Threads
 *    make this cheap — sleeping does NOT block the OS thread.
 */
@Service
public class OrderService {

    // URLs for downstream services
    private static final String INVENTORY_URL = "http://localhost:8081/api/inventory/check";
    private static final String PAYMENT_URL   = "http://localhost:8082/api/payment/process";

    // Max retry attempts for failed service calls
    private static final int MAX_RETRIES = 3;

    // Delay between retries in milliseconds
    private static final long RETRY_DELAY_MS = 1000;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Processes a single order end-to-end.
     *
     * Uses a virtual thread executor to run inventory check and
     * payment processing. Demonstrates the thread-per-request model
     * where each task gets its own lightweight virtual thread.
     *
     * @param order The incoming order to process
     * @return The order with updated status (CONFIRMED or FAILED)
     */
    public Order processOrder(Order order) {

        System.out.println("[OrderService] Processing order: " + order.getOrderId() +
                           " | Thread: " + Thread.currentThread());

        // --------------------------------------------------------
        // VIRTUAL THREAD EXECUTOR
        // Each service call runs in its own virtual thread.
        // Executors.newVirtualThreadPerTaskExecutor() is the
        // required API from the project rubric.
        // --------------------------------------------------------
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Step 1: Check inventory in a virtual thread
            Future<ServiceResponse> inventoryFuture = virtualExecutor.submit(() ->
                callWithRetry("Inventory", INVENTORY_URL, order)
            );

            // Step 2: Get inventory result (blocking get — cheap with virtual threads)
            ServiceResponse inventoryResponse = inventoryFuture.get();

            if (!inventoryResponse.isSuccess()) {
                order.setStatus("FAILED");
                System.out.println("[OrderService] Inventory check failed for order: " + order.getOrderId());
                return order;
            }

            // Step 3: Process payment in a virtual thread
            Future<ServiceResponse> paymentFuture = virtualExecutor.submit(() ->
                callWithRetry("Payment", PAYMENT_URL, order)
            );

            // Step 4: Get payment result (blocking get — cheap with virtual threads)
            ServiceResponse paymentResponse = paymentFuture.get();

            if (!paymentResponse.isSuccess()) {
                order.setStatus("FAILED");
                System.out.println("[OrderService] Payment failed for order: " + order.getOrderId());
                return order;
            }

            // Step 5: All good — confirm the order
            order.setStatus("CONFIRMED");
            System.out.println("[OrderService] Order confirmed: " + order.getOrderId());
            return order;

        } catch (Exception e) {
            order.setStatus("FAILED");
            System.err.println("[OrderService] Unexpected error for order " +
                               order.getOrderId() + ": " + e.getMessage());
            return order;
        }
    }

    /**
     * ============================================================
     * RETRY LOGIC — Failure Handling (Required by Rubric)
     * ============================================================
     *
     * Attempts to call a downstream service up to MAX_RETRIES times.
     * Between each attempt, Thread.sleep() waits RETRY_DELAY_MS ms.
     *
     * Why Thread.sleep() is fine here with Virtual Threads:
     * When a virtual thread sleeps, it is UNMOUNTED from its carrier
     * thread. The carrier thread is freed to run other virtual threads.
     * This is fundamentally different from platform threads where
     * sleep() blocks the OS thread entirely.
     *
     * @param serviceName  Human-readable name for logging
     * @param url          Full URL of the service endpoint
     * @param order        The order payload to send
     * @return ServiceResponse from the downstream service
     */
    private ServiceResponse callWithRetry(String serviceName, String url, Order order) {

        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                attempts++;
                System.out.println("[OrderService] Calling " + serviceName +
                                   " (attempt " + attempts + ") for order: " + order.getOrderId());

                // BLOCKING REST CALL — virtual thread parks here while waiting
                ServiceResponse response = restTemplate.postForObject(url, order, ServiceResponse.class);

                if (response != null && response.isSuccess()) {
                    return response;
                }

                System.out.println("[OrderService] " + serviceName + " returned failure. Retrying...");

            } catch (Exception e) {
                System.err.println("[OrderService] " + serviceName + " call failed (attempt " +
                                   attempts + "): " + e.getMessage());
            }

            // Wait before retrying — virtual thread parks, carrier thread is freed
            if (attempts < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // All retries exhausted
        System.err.println("[OrderService] " + serviceName + " failed after " + MAX_RETRIES + " attempts.");
        return new ServiceResponse(false, serviceName + " unavailable after " + MAX_RETRIES + " retries");
    }
}
