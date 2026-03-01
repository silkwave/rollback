package com.example.rollback.retry;

/**
 * 재시도 정책(여부/대기시간)을 정의합니다.
 */
public interface RetryStrategy {
    
    /**
     * 재시도 여부를 결정합니다.
     */
    boolean shouldRetry(Exception ex, int attemptCount);
    
    /**
     * 다음 재시도까지 대기 시간을 계산합니다.
     */
    long getWaitTime(int attemptCount);
} 
