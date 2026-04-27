package com.bit313.service;

import com.bit313.model.Order;
import com.bit313.model.ServiceResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ============================================================
 * PAYMENT SERVICE — Business Logic
 * ============================================================
 *
 * Simulates payment gateway processing.
 *
 * In production this would call a payment provider (e.g. Stripe).
 * Here we simulate:
 * - A 100ms processing delay (payment gateway latency)
 * - A 5% random failure rate (simulates declined payments)
 * - An atomic transaction counter for metrics
 *
 * The 100ms delay is important for Phase 3 benchmarking:
 * - With platform threads: 100ms sleep blocks the OS thread
 * - With virtual threads: 100ms sleep parks the VT, freeing
 *   the carrier thread to handle other concurrent requests
 *
 * AtomicLong is used for the transaction counter because
 * multiple virtual threads increment it simultaneously.
 */
@Service
public class PaymentService {

    // Atomic counter — thread-safe increment for concurrent virtual threads
    private final AtomicLong transactionCount = new AtomicLong(0);

    // Simulated failure rate: 5% of payments fail (realistic scenario)
    private static final double FAILURE_RATE = 0.05;

    /**
     * Processes payment for an order.
     * Simulates 100ms payment gateway latency.
     *
     * @param order The order to charge
     * @return ServiceResponse indicating payment success or failure
     */
    public ServiceResponse processPayment(Order order) {

        long txId = transactionCount.incrementAndGet();

        System.out.println("[PaymentService] Processing payment TX#" + txId +
                           " for order: " + order.getOrderId() +
                           " | Amount: RM" + order.getTotalPrice() +
                           " | Thread: " + Thread.currentThread());

        // Simulate payment gateway network call (100ms)
        // Virtual thread parks here — carrier thread is freed for other work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ServiceResponse(false, "Payment interrupted");
        }

        // Simulate 5% payment failure (declined card, gateway error, etc.)
        if (Math.random() < FAILURE_RATE) {
            System.out.println("[PaymentService] Payment declined TX#" + txId);
            return new ServiceResponse(false, "Payment declined for order: " + order.getOrderId());
        }

        System.out.println("[PaymentService] Payment successful TX#" + txId +
                           " | Total processed: " + txId);

        return new ServiceResponse(true,
            "Payment successful. TX#" + txId + " | Amount: RM" + order.getTotalPrice());
    }

    /**
     * Returns total number of payment transactions processed.
     * Useful for Phase 3 metrics reporting.
     */
    public long getTotalTransactions() {
        return transactionCount.get();
    }
}
