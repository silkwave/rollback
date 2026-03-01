package com.example.rollback.exception;

/**
 * 결제/정산 처리에서 사용하는 비즈니스 예외입니다.
 */
public class PaymentException extends RuntimeException {
    
    /**
     * 메시지로 예외를 생성합니다.
     */
    public PaymentException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인 예외로 예외를 생성합니다.
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
