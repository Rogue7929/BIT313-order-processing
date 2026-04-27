package com.bit313.model;

public class Order {
    private String orderId;
    private String productId;
    private int quantity;
    private double totalPrice;
    private String status;

    public Order() {}

    public String getOrderId()               { return orderId; }
    public void setOrderId(String orderId)   { this.orderId = orderId; }
    public String getProductId()             { return productId; }
    public void setProductId(String id)      { this.productId = id; }
    public int getQuantity()                 { return quantity; }
    public void setQuantity(int q)           { this.quantity = q; }
    public double getTotalPrice()            { return totalPrice; }
    public void setTotalPrice(double p)      { this.totalPrice = p; }
    public String getStatus()               { return status; }
    public void setStatus(String status)    { this.status = status; }
}
