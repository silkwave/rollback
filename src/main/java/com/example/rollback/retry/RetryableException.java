package com.example.rollback.retry;

/**
 * 재시도 대상임을 명시하는 런타임 예외입니다.
 * 일시적 장애에 대해 재시도를 유도할 때 사용합니다.
 */
public class RetryableException extends RuntimeException {
    
    /**
     * 메시지로 예외를 생성합니다.
     */
    public RetryableException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인 예외로 예외를 생성합니다.
     */
    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인 예외로 예외를 생성합니다.
     */
    public RetryableException(Throwable cause) {
        super(cause);
    }
}
