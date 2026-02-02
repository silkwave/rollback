package com.example.rollback.retry.strategy;

/**
 * 재시도 가능 여부를 판단하는 조건 인터페이스
 * 
 * <p>이 인터페이스는 <strong>컴포지트 패턴(Composite Pattern)</strong>의 일부로,
 * 개별적인 재시도 조건을 정의합니다. 여러 조건을 조합하여 복잡한 재시도 규칙을 
 * 구성할 수 있습니다.</p>
 * 
 * <p><strong>컴포지트 패턴(Composite Pattern)이란?</strong></p>
 * <p>개별 객체와 복합 객체를 동일하게 처리할 수 있게 하는 구조 패턴입니다. 
 * 이 패턴을 통해 다음과 같은 이점을 얻을 수 있습니다:</p>
 * <ul>
 *   <li><strong>일관성:</strong> 개별 조건과 조합 조건을 동일한 인터페이스로 처리</li>
 *   <li><strong>확장성:</strong> 새로운 조건을 쉽게 추가하고 기존 조건과 조합 가능</li>
 *   <li><strong>재사용성:</strong> 개별 조건을 독립적으로 재사용하고 테스트 가능</li>
 * </ul>
 * 
 * <p><strong>주요 구현체:</strong></p>
 * <ul>
 *   <li>{@link LockRetryCondition} - 락 충돌 조건</li>
 *   <li>{@link DeadlockRetryCondition} - 데드락 조건</li>
 *   <li>TimeoutRetryCondition - 타임아웃 조건 (예시)</li>
 *   <li>NetworkRetryCondition - 네트워크 관련 조건 (예시)</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 여러 조건을 조합하여 사용
 * List<RetryCondition> conditions = Arrays.asList(
 *     new LockRetryCondition(),
 *     new DeadlockRetryCondition(),
 *     new TimeoutRetryCondition()
 * );
 * 
 * // RandomBackoffRetryStrategy에 조건 리스트 주입
 * RetryStrategy strategy = new RandomBackoffRetryStrategy(conditions);
 * }</pre>
 * 
 * @see LockRetryCondition
 * @see DeadlockRetryCondition
 * @see com.example.rollback.retry.strategy.RandomBackoffRetryStrategy
 */
public interface RetryCondition {
    
    /**
     * 주어진 예외가 재시도 가능한 조건인지 판단합니다.
     * 
     * @param t 판단할 예외 객체 (null 가능)
     * @return 재시도 가능하면 true, 그렇지 않으면 false
     */
    boolean isRetryable(Throwable t);
}
