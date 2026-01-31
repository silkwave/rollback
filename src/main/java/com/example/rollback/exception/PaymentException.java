package com.example.rollback.exception;

public class PaymentException extends OrderException {
    
    public PaymentException(String message) {
        super("PAYMENT_ERROR", message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super("PAYMENT_ERROR", message, cause);
    }
}