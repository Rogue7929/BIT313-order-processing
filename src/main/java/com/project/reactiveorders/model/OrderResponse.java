package com.project.reactiveorders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for order responses sent back to clients
 * Contains order status, ID, and processing metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private String orderId;
    private String status;  // SUCCESS, FAILED_PAYMENT, OUT_OF_STOCK, ERROR
    private String message;
    private Long processingTimeMs;
    private Double totalPrice;
}
