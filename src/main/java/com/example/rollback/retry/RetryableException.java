package com.example.rollback.retry;

/**
 * 재시도 가능한 예외를 나타내는 클래스
 * 
 * <p>이 클래스는 애플리케이션 레벨에서 명시적으로 재시도가 필요한 예외 상황을
 * 표현하기 위해 사용됩니다. 예를 들어, 비즈니스 로직에서 일시적인 장애가 발생하여
 * 재시도가 필요한 경우 이 예외를 발생시킬 수 있습니다.</p>
 * 
 * <p><strong>주요 사용 시나리오:</strong></p>
 * <ul>
 *   <li><strong>외부 API 호출 실패:</strong> 일시적인 네트워크 장애나 서버 과부하</li>
 *   <li><strong>리소스 경합:</strong> 동시성 제어로 인한 일시적인 자원 접근 실패</li>
 *   <li><strong>트랜잭션 충돌:</strong> 낙관적/비관적 락 충돌 상황</li>
 *   <li><strong>임계치 초과:</strong> 속도 제한(rate limiting)으로 인한 잠시 대기 필요</li>
 * </ul>
 * 
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li><strong>명시적 표현:</strong> 재시도 필요성을 명확하게 표현</li>
 *   <li><strong>표준 예외 계층:</strong> RuntimeException을 상속하여 체크 예외 아님</li>
 *   <li><strong>유연성:</strong> 메시지, 원인 예외 등 다양한 생성자 제공</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 1. 간단한 메시지로 예외 발생
 * throw new RetryableException("외부 API 서버 일시적 장애");
 * 
 * // 2. 원인 예외와 함께 발생
 * try {
 *     externalService.call();
 * } catch (ServiceUnavailableException e) {
 *     throw new RetryableException("서비스 일시적 불가", e);
 * }
 * 
 * // 3. 원인 예외만 포함
 * catch (TimeoutException e) {
 *     throw new RetryableException(e);
 * }
 * }</pre>
 * 
 * <p><strong>LockRetryTemplate과의 연동:</strong></p>
 * <pre>{@code
 * // RetryableException을 명시적으로 처리
 * public <T> T execute(Supplier<T> action) {
 *     try {
 *         return action.get();
 *     } catch (RetryableException e) {
 *         // 명시적인 재시도 가능 예외 처리
 *         if (shouldRetry(e)) {
 *             return retryAfterWait();
 *         }
 *         throw e;
 *     }
 * }
 * }</pre>
 * 
 * @author Rollback Team
 * @since 1.0
 * @see LockRetryTemplate 재시도 템플릿 클래스
 * @see RetryStrategy 재시도 전략 인터페이스
 */
public class RetryableException extends RuntimeException {
    
    /**
     * 상세 메시지를 가진 RetryableException을 생성합니다.
     * 
     * @param message 예외에 대한 상세 설명 메시지
     */
    public RetryableException(String message) {
        super(message);
    }
    
    /**
     * 상세 메시지와 원인 예외를 가진 RetryableException을 생성합니다.
     * 
     * @param message 예외에 대한 상세 설명 메시지
     * @param cause 이 예외를 발생시킨 원인 예외
     */
    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인 예외만 가진 RetryableException을 생성합니다.
     * 
     * @param cause 이 예외를 발생시킨 원인 예외
     */
    public RetryableException(Throwable cause) {
        super(cause);
    }
}