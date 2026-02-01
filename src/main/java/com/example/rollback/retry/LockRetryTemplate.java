package com.example.rollback.retry;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
@Component
public class LockRetryTemplate {

        
    private final RetryStrategy retryStrategy;
    
    public LockRetryTemplate(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
    
    public <T> T execute(Supplier<T> action) {
        int attempt = 0;
        
        while (true) {
            attempt++;
            try {
                log.debug("작업 실행 시도: {}", attempt);
                return action.get();
                
            } catch (Exception e) {
                log.warn("작업 실패 (시도: {}): {}", attempt, e.getMessage());
                
                if (retryStrategy.shouldRetry(e, attempt)) {
                    long waitTime = retryStrategy.getWaitTime(attempt);
                    log.info("재시도 대기: {}ms (시도: {}/{}), 예외: {}", 
                        waitTime, attempt, 5, e.getClass().getSimpleName());
                    
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("재시도 대기 중 인터럽트 발생", ie);
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                    
                    continue;
                } else {
                    log.error("재시도 최종 실패 (시도: {}): {}", attempt, e.getMessage());
                    throw e;
                }
            }
        }
    }
}