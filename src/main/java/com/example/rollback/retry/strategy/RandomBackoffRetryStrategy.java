package com.example.rollback.retry.strategy;

import com.example.rollback.retry.RetryStrategy;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 백오프(지터 포함) 기반 재시도 전략입니다.
 * 재시도 대상이면 대기 후 재시도합니다.
 */
@Slf4j
public class RandomBackoffRetryStrategy implements RetryStrategy {

    /** 최대 재시도 횟수 */
    private final int maxRetries;
    
    /** 기본 대기 시간 (밀리초) */
    private final long baseWaitMs;
    
    /** 최대 랜덤 지터 시간 (밀리초) */
    private final long maxJitterMs;
    
    /** 최대 대기 시간 (밀리초) */
    private final long maxWaitMs;
    
    /** 재시도 조건 */
    private final RetryCondition retryCondition;

    /**
     * 기본 설정값으로 생성합니다.
     */
    public RandomBackoffRetryStrategy(RetryCondition retryCondition) {
        this(10, 100, 200, 2000, retryCondition);
    }

    /**
     * 설정값을 지정하여 생성합니다.
     */
    public RandomBackoffRetryStrategy(int maxRetries, long baseWaitMs, long maxJitterMs, 
                                     long maxWaitMs, RetryCondition retryCondition) {
        this.maxRetries = maxRetries;
        this.baseWaitMs = baseWaitMs;
        this.maxJitterMs = maxJitterMs;
        this.maxWaitMs = maxWaitMs;
        this.retryCondition = retryCondition;
        log.info("RandomBackoffRetryStrategy 활성화 (최대 재시도: {}회)", maxRetries);
    }

    /**
     * 재시도 여부를 판단합니다.
     */
    @Override
    public boolean shouldRetry(Exception ex, int attemptCount) {
        if (attemptCount >= maxRetries) {
            log.warn("최대 재시도 횟수({}) 초과로 중단합니다. (시도 횟수: {})", maxRetries, attemptCount);
            return false;
        }
        
        // 재시도 대상 판단
        boolean shouldRetry = retryCondition.isRetryable(ex);
        
        if (shouldRetry) {
            log.debug("재시도 조건 만족 - {} (시도 횟수: {}/{})", 
                     ex.getClass().getSimpleName(), attemptCount, maxRetries);
        } else {
            log.debug("재시도 조건 불만족 - {} (시도 횟수: {}/{})", 
                     ex.getClass().getSimpleName(), attemptCount, maxRetries);
        }
        
        return shouldRetry;
    }

    /**
     * 다음 재시도까지 대기 시간을 계산합니다.
     */
    @Override
    public long getWaitTime(int attemptCount) {
        // 기본 + 가중치 + 지터 (상한 적용)
        long wait = Math.min(
            baseWaitMs + (attemptCount * 50L) + ThreadLocalRandom.current().nextLong(maxJitterMs), 
            maxWaitMs
        );
        
        log.debug("재시도 대기 시간 계산: {}ms (시도 횟수: {}, 기본: {}ms, 가중치: {}ms)", 
                 wait, attemptCount, baseWaitMs, attemptCount * 50);
        
        return wait;
    }
}
