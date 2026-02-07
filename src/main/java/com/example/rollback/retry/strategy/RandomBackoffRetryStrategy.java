package com.example.rollback.retry.strategy;

import com.example.rollback.retry.RetryStrategy;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 백오프(Random Backoff) 재시도 전략 클래스
 * 
 * <p>이 클래스는 RetryStrategy 인터페이스를 구현하여, 랜덤 백오프 알고리즘을 
 * 사용한 지능적인 재시도 정책을 제공합니다. 전략 패턴의 구체적인 전략 구현체이며,
 * 단일 RetryCondition 객체를 활용하여 재시도 조건을 판단합니다.</p>
 * 
 * <p><strong>랜덤 백오프란?</strong></p>
 * <p>재시도 간격을 랜덤하게 증가시키는 기법으로, 다음과 같은 이점이 있습니다:</p>
 * <ul>
 *   <li><strong>썬더링 헤드 방지:</strong> 여러 클라이언트가 동시에 재시도하는 것을 방지</li>
 *   <li><strong>부하 분산:</b> 시스템에 가해지는 부하를 시간적으로 분산</li>
 *   <li><strong>적응성:</strong> 실패 횟수에 따라 점진적으로 대기 시간 증가</li>
 * </ul>
 * 
 * <p><strong>RetryCondition 통합:</strong></p>
 * <p>기존에 여러 RetryCondition을 리스트로 받아 처리하던 방식에서, 이제는
 * LockRetryCondition과 DeadlockRetryCondition의 기능이 통합된 단일
 * RetryCondition 객체를 통해 재시도 여부를 판단합니다. 이로써 재시도 조건 로직이
 * 한 곳으로 집중되어 관리됩니다.</p>
 * 
 * <p><strong>대기 시간 계산 공식:</strong></p>
 * <pre>{@code
 * waitTime = min(baseWaitMs + (attemptCount * 50) + random(0, maxJitterMs), maxWaitMs)
 * }</pre>
 * 
 * <p><strong>설정 파라미터:</strong></p>
 * <ul>
 *   <li>maxRetries: 최대 재시도 횟수 (기본값: 10)</li>
 *   <li>baseWaitMs: 기본 대기 시간 (기본값: 100ms)</li>
 *   <li>maxJitterMs: 최대 지터 시간 (기본값: 200ms)</li>
 *   <li>maxWaitMs: 최대 대기 시간 (기본값: 2000ms)</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Spring Configuration에서 설정
 * @Bean
 * public RetryStrategy retryStrategy(RetryCondition retryCondition) {
 *     return new RandomBackoffRetryStrategy(5, 200, 500, 3000, retryCondition);
 * }
 * 
 * // 또는 기본 설정 사용
 * @Bean
 * public RetryStrategy retryStrategy(RetryCondition retryCondition) {
 *     return new RandomBackoffRetryStrategy(retryCondition);
 * }
 * }</pre>
 * 
 * @author Rollback Team
 * @since 1.0
 * @see RetryStrategy 재시도 전략 인터페이스
 * @see RetryCondition 재시도 조건 클래스
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
     * 기본 설정값으로 RandomBackoffRetryStrategy를 생성하는 생성자
     * 
     * <p>기본 설정값:</p>
     * <ul>
     *   <li>최대 재시도 횟수: 10회</li>
     *   <li>기본 대기 시간: 100ms</li>
     *   <li>최대 지터 시간: 200ms</li>
     *   <li>최대 대기 시간: 2000ms</li>
     * </ul>
     * 
     * @param retryCondition 재시도 조건 객체
     */
    public RandomBackoffRetryStrategy(RetryCondition retryCondition) {
        this(10, 100, 200, 2000, retryCondition);
    }

    /**
     * 모든 설정값을 지정하여 RandomBackoffRetryStrategy를 생성하는 생성자
     * 
     * @param maxRetries 최대 재시도 횟수
     * @param baseWaitMs 기본 대기 시간 (밀리초)
     * @param maxJitterMs 최대 랜덤 지터 시간 (밀리초)
     * @param maxWaitMs 최대 대기 시간 (밀리초)
     * @param retryCondition 재시도 조건 객체
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
     * 예외 발생 시 재시도 여부를 결정합니다.
     * 
     * <p>재시도 여부는 다음과 같은 순서로 판단됩니다:</p>
     * <ol>
     *   <li>최대 재시도 횟수 초과 여부 확인</li>
     *   <li>등록된 RetryCondition 객체가 true를 반환하면 재시도</li>
     * </ol>
     * 
     * @param e 발생한 예외
     * @param attemptCount 현재까지의 시도 횟수 (1부터 시작)
     * @return 재시도해야 하면 true, 그렇지 않으면 false
     */
    @Override
    public boolean shouldRetry(Exception ex, int attemptCount) {
        if (attemptCount >= maxRetries) {
            log.warn("최대 재시도 횟수({}) 초과로 중단합니다. (시도 횟수: {})", maxRetries, attemptCount);
            return false;
        }
        
        // 통합된 RetryCondition 객체를 통해 재시도 여부 판단
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
     * 다음 재시도까지의 대기 시간을 계산합니다.
     * 
     * <p>대기 시간은 다음 공식으로 계산됩니다:</p>
     * <pre>{@code
     * waitTime = min(baseWaitMs + (attemptCount * 50) + random(0, maxJitterMs), maxWaitMs)
     * }</pre>
     * 
     * <p>이 공식을 통해 다음과 같은 특성을 가집니다:</p>
     * <ul>
     *   <li><strong>점진적 증가:</strong> 시도 횟수에 따라 기본 대기 시간 증가</li>
     *   <li><strong>랜덤성:</strong> 지터를 통해 클라이언트 간 동기화 방지</li>
     *   <li><strong>상한선:</strong> 최대 대기 시간을 초과하지 않음</li>
     * </ul>
     * 
     * @param attemptCount 현재 시도 횟수 (1부터 시작)
     * @return 대기 시간 (밀리초 단위)
     */
    @Override
    public long getWaitTime(int attemptCount) {
        // 대기 시간 = 기본값 + (회차별 가중치) + 랜덤 지터(0~maxJitter)
        long wait = Math.min(
            baseWaitMs + (attemptCount * 50L) + ThreadLocalRandom.current().nextLong(maxJitterMs), 
            maxWaitMs
        );
        
        log.debug("재시도 대기 시간 계산: {}ms (시도 횟수: {}, 기본: {}ms, 가중치: {}ms)", 
                 wait, attemptCount, baseWaitMs, attemptCount * 50);
        
        return wait;
    }
}
