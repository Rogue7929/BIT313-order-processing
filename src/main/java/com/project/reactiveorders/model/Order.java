package com.project.reactiveorders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal Order domain model
 * Represents the complete state of an order during processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String productId;
    private Integer quantity;
    private Double price;
    private String status;  // CREATED, INVENTORY_RESERVED, PAYMENT_PROCESSED, COMPLETED, FAILED
    private Long createdAt;
    private Long startTime;
    private Long endTime;
}
