package com.project.reactiveorders.exception;

/**
 * Custom exception for payment-related errors
 * Thrown when payment processing fails or times out
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
