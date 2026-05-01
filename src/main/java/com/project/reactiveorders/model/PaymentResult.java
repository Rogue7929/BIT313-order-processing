package com.project.reactiveorders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment result model
 * Represents the result of a payment transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private String transactionId;
    private String status;  // SUCCESS, FAILED, TIMEOUT
    private String message;
    private Long processingTimeMs;
}
