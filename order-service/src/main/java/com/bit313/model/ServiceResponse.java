package com.bit313.model;

/**
 * Standardized response object returned by Inventory and Payment services.
 * Wraps success/failure status and a message for the Order Service to act on.
 */
public class ServiceResponse {

    private boolean success;
    private String message;

    public ServiceResponse() {}

    public ServiceResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess()               { return success; }
    public void setSuccess(boolean success)  { this.success = success; }

    public String getMessage()               { return message; }
    public void setMessage(String message)   { this.message = message; }
}
