package com.example.rollback.retry;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 락(Lock) 재시도 템플릿 클래스
 * 
 * <p>이 클래스는 템플릿 메서드 패턴을 구현하여, 데이터베이스 락 충돌이나 
 * 데드락과 같은 일시적인 장애 상황에서 자동으로 재시도를 수행합니다.</p>
 * 
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>주입된 RetryStrategy를 통해 재시도 여부와 대기 시간 결정</li>
 *   <li>인터럽트 처리를 통한 안전한 스레드 관리</li>
 *   <li>상세한 로깅을 통한 디버깅 및 모니터링 지원</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Spring에서 자동 주입된 경우
 * @Autowired private LockRetryTemplate retryTemplate;
 * 
 * // 사용
 * User user = retryTemplate.execute(() -> userService.findById(userId));
 * }</pre>
 * 
 * @see RetryStrategy 재시도 전략 인터페이스
 * @see com.example.rollback.retry.strategy.RandomBackoffRetryStrategy 랜덤 백오프 전략 구현체
 */
@Slf4j
@Component
public class LockRetryTemplate {

    /**
     * 주입된 재시도 전략
     * 전략 패턴(Strategy Pattern)을 통해 재시도 방식을 유연하게 변경할 수 있습니다.
     */
    private final RetryStrategy retryStrategy;
    
    /**
     * LockRetryTemplate 생성자
     * @param retryStrategy 재시도 전략 객체 (예: RandomBackoffRetryStrategy)
     */
    public LockRetryTemplate(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
    
    /**
     * 재시도 로직이 적용된 작업을 실행하는 메서드
     * 
     * <p>이 메서드는 템플릿 메서드 패턴을 구현하여 다음과 같은 순서로 작업을 처리합니다:</p>
     * <ol>
     *   <li>전달된 작업(action)을 실행</li>
     *   <li>예외가 발생하면 RetryStrategy를 통해 재시도 여부 판단</li>
     *   <li>재시도가 필요하면 지정된 대기 시간만큼 대기 후 재시도</li>
     *   <li>최대 재시도 횟수를 초과하거나 재시도 불가능한 예외면 예외 재전파</li>
     * </ol>
     * 
     * @param <T> 반환 타입
     * @param action 실행할 작업을 나타내는 Supplier 함수
     * @return 작업 성공 시 반환 결과
     * @throws RuntimeException 작업 실패 또는 인터럽트 발생 시
     */
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