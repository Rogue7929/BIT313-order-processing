package com.bit313.model;

/**
 * Represents an order in the e-commerce system.
 * Used as the request/response model across all three services.
 */
public class Order {

    private String orderId;
    private String productId;
    private int quantity;
    private double totalPrice;
    private String status; // PENDING, CONFIRMED, FAILED

    public Order() {}

    public Order(String orderId, String productId, int quantity, double totalPrice) {
        this.orderId    = orderId;
        this.productId  = productId;
        this.quantity   = quantity;
        this.totalPrice = totalPrice;
        this.status     = "PENDING";
    }

    // Getters and Setters
    public String getOrderId()               { return orderId; }
    public void setOrderId(String orderId)   { this.orderId = orderId; }

    public String getProductId()                 { return productId; }
    public void setProductId(String productId)   { this.productId = productId; }

    public int getQuantity()              { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice()                 { return totalPrice; }
    public void setTotalPrice(double totalPrice)  { this.totalPrice = totalPrice; }

    public String getStatus()               { return status; }
    public void setStatus(String status)    { this.status = status; }

    @Override
    public String toString() {
        return "Order{orderId='" + orderId + "', productId='" + productId +
               "', quantity=" + quantity + ", totalPrice=" + totalPrice +
               ", status='" + status + "'}";
    }
}
