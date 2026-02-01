package com.example.rollback.retry;

import com.example.rollback.exception.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinearBackoffRetryStrategy implements RetryStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(LinearBackoffRetryStrategy.class);
    
    private final int maxAttempts;
    private final long initialDelay;
    private final long increment;
    private final Class<? extends Exception>[] retryableExceptions;
    
    public LinearBackoffRetryStrategy(int maxAttempts, long initialDelay, long increment) {
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        this.increment = increment;
        this.retryableExceptions = new Class[]{PaymentException.class};
    }
    
    @Override
    public boolean shouldRetry(Exception e, int attemptCount) {
        if (attemptCount >= maxAttempts) {
            log.debug("재시도 최대 횟수 도달: {}/{}", attemptCount, maxAttempts);
            return false;
        }
        
        for (Class<? extends Exception> retryableException : retryableExceptions) {
            if (retryableException.isInstance(e)) {
                log.debug("재시도 가능한 예외 감지: {} (시도: {}/{})", 
                    e.getClass().getSimpleName(), attemptCount, maxAttempts);
                return true;
            }
        }
        
        log.debug("재시도 불가능한 예외: {} (시도: {}/{})", 
            e.getClass().getSimpleName(), attemptCount, maxAttempts);
        return false;
    }
    
    @Override
    public long getWaitTime(int attemptCount) {
        long waitTime = initialDelay + (increment * (attemptCount - 1));
        log.debug("대기 시간 계산: {}ms (시도: {})", waitTime, attemptCount);
        return waitTime;
    }
}