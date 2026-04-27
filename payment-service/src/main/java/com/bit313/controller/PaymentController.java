package com.bit313.controller;

import com.bit313.model.Order;
import com.bit313.model.ServiceResponse;
import com.bit313.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * PAYMENT CONTROLLER — REST API
 * ============================================================
 *
 * Exposes: POST /api/payment/process
 *
 * Called by Order Service after a successful inventory check
 * to charge the customer for their order.
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Process payment for an order.
     *
     * POST /api/payment/process
     * Body: Order JSON
     * Returns: ServiceResponse (success true/false + TX details)
     */
    @PostMapping("/process")
    public ResponseEntity<ServiceResponse> processPayment(@RequestBody Order order) {
        ServiceResponse response = paymentService.processPayment(order);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     * Also returns total transactions processed — useful for Phase 3 metrics.
     *
     * GET /api/payment/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(
            "Payment Service is running | " +
            "Total transactions: " + paymentService.getTotalTransactions() +
            " | Thread: " + Thread.currentThread()
        );
    }
}
