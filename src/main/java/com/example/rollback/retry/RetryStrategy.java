package com.example.rollback.retry;

/**
 * 재시도 전략을 정의하는 인터페이스
 * 
 * <p>이 인터페이스는 <strong>전략 패턴(Strategy Pattern)</strong>을 구현하며,
 * 재시도 로직을 독립적인 전략으로 분리하여 유연성과 확장성을 제공합니다.</p>
 * 
 * <p><strong>전략 패턴(Strategy Pattern)이란?</strong></p>
 * <p>알고리즘을 객체로 캡슐화하여, 클라이언트가 실행 중에 알고리즘을 선택하거나 
 * 변경할 수 있게 하는 행동 패턴입니다. 이를 통해 다음과 같은 이점을 얻을 수 있습니다:</p>
 * <ul>
 *   <li><strong>개방-폐쇄 원칙(OCP):</strong> 새로운 재시도 전략 추가 시 기존 코드 수정 불필요</li>
 *   <li><strong>유연성:</strong> 런타임에 재시도 전략 동적 변경 가능</li>
 *   <li><strong>테스트 용이성:</strong> 목 객체를 통한 단위 테스트 수월</li>
 * </ul>
 * 
 * <p><strong>주요 구현체:</strong></p>
 * <ul>
 *   <li>{@link com.example.rollback.retry.strategy.RandomBackoffRetryStrategy} - 랜덤 백오프 전략</li>
 *   <li>FixedWaitRetryStrategy - 고정 대기 시간 전략 (예시)</li>
 *   <li>ExponentialBackoffRetryStrategy - 지수 백오프 전략 (예시)</li>
 * </ul>
 * 
 * @see com.example.rollback.retry.strategy.RandomBackoffRetryStrategy
 */
public interface RetryStrategy {
    
    /**
     * 예외 발생 시 재시도 여부를 결정합니다.
     * 
     * @param e 발생한 예외
     * @param attemptCount 현재까지의 시도 횟수 (1부터 시작)
     * @return 재시도해야 하면 true, 그렇지 않으면 false
     */
    boolean shouldRetry(Exception e, int attemptCount);
    
    /**
     * 다음 재시도까지의 대기 시간을 계산합니다.
     * 
     * @param attemptCount 현재 시도 횟수 (1부터 시작)
     * @return 대기 시간 (밀리초 단위)
     */
    long getWaitTime(int attemptCount);
}