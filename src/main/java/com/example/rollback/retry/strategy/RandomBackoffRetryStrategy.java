package com.example.rollback.retry.strategy;

import com.example.rollback.retry.RetryStrategy;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 백오프(Random Backoff) 전략
 * 예외 판단은 주입된 RetryCondition 리스트에 위임하여 확장성을 높였습니다.
 */
@Slf4j
public class RandomBackoffRetryStrategy implements RetryStrategy {

    private final int maxRetries;
    private final long baseWaitMs;
    private final long maxJitterMs;
    private final long maxWaitMs;
    private final List<RetryCondition> conditions;

    // 기본 설정값 생성자
    public RandomBackoffRetryStrategy(List<RetryCondition> conditions) {
        this(10, 100, 200, 2000, conditions);
    }

    public RandomBackoffRetryStrategy(int maxRetries, long baseWaitMs, long maxJitterMs, 
                                     long maxWaitMs, List<RetryCondition> conditions) {
        this.maxRetries = maxRetries;
        this.baseWaitMs = baseWaitMs;
        this.maxJitterMs = maxJitterMs;
        this.maxWaitMs = maxWaitMs;
        this.conditions = conditions;
        log.info("RandomBackoffRetryStrategy 활성화 (적용된 조건: {}개)", conditions.size());
    }

    @Override
    public boolean shouldRetry(Exception e, int attemptCount) {
        if (attemptCount >= maxRetries) {
            log.warn("최대 재시도 횟수({}) 초과로 중단합니다.", maxRetries);
            return false;
        }
        // 등록된 조건 중 하나라도 만족하면 재시도
        return conditions.stream().anyMatch(c -> c.isRetryable(e));
    }

    @Override
    public long getWaitTime(int attemptCount) {
        // 대기 시간 = 기본값 + (회차별 가중치) + 랜덤 지터(0~maxJitter)
        long wait = Math.min(baseWaitMs + (attemptCount * 50L) + 
                    ThreadLocalRandom.current().nextLong(maxJitterMs), maxWaitMs);
        
        log.debug("재시도 대기 시간: {}ms (시도 횟수: {})", wait, attemptCount);
        return wait;
    }
}
