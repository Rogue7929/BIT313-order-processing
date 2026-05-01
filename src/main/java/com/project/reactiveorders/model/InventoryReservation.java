package com.project.reactiveorders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inventory reservation result model
 * Represents the result of attempting to reserve stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {
    private String productId;
    private Integer reservedQuantity;
    private Boolean success;
    private String message;
}
