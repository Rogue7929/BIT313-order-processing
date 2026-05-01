package com.project.reactiveorders.exception;

/**
 * Custom exception for inventory-related errors
 * Thrown when stock is unavailable for requested products
 */
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
