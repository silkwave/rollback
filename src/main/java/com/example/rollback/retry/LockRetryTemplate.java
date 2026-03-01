package com.example.rollback.retry;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * 재시도(백오프 포함) 실행을 담당하는 템플릿입니다.
 * 트랜잭션은 호출자가 관리하고, 여기서는 재시도/대기만 처리합니다.
 */
@Slf4j
@Component
public class LockRetryTemplate {

    /**
     * 재시도 전략입니다.
     */
    private final RetryStrategy retryStrategy;

    /**
     * 생성자입니다.
     */
    public LockRetryTemplate(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
    
    /**
     * 재시도 로직이 적용된 작업을 실행하는 메서드
     *
     * @param action 실행할 작업
     */
    public <T> T execute(Supplier<T> action) {
        return execute(action, null);
    }

    /**
     * 재시도 로직이 적용된 작업을 실행하는 메서드 (최종 실패 훅 제공)
     *
     * @param action 실행할 작업
     * @param onFinalFailure 최종 실패 시 1회 호출되는 훅 (null 가능)
     */
    public <T> T execute(Supplier<T> action, Consumer<Exception> onFinalFailure) {
        int attempt = 0;
        
        while (true) {
            attempt++;
            
            try {
                log.debug("");                
                log.debug("");                
                log.debug("");                
                log.debug("");                
                log.debug("");    
                log.debug("==============================================================");
                                                                            

                log.debug("작업 실행 시도: {}", attempt);
                return action.get();
                
            } catch (Exception ex) {
                boolean shouldRetry = retryStrategy.shouldRetry(ex, attempt);
                if (!shouldRetry && onFinalFailure != null) {
                    try {
                        onFinalFailure.accept(ex);
                    } catch (Exception hookEx) {
                        log.warn("최종 실패 훅(onFinalFailure) 처리 중 예외 발생: {}", hookEx.getClass().getSimpleName());
                    }
                }
                
                log.warn("작업 실패 (시도: {}): {}", attempt, ex.getClass().getSimpleName());
                
                if (shouldRetry) {
                    long waitTime = retryStrategy.getWaitTime(attempt);
                    log.info("재시도 대기: {}ms (시도: {}), 예외: {}", 
                        waitTime, attempt, ex.getClass().getSimpleName());
                    
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("재시도 대기 중 인터럽트 발생", ie);
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                    
                    continue;
                } else {
                    log.error("재시도 최종 실패 (시도: {}): {}", attempt, ex.getClass().getSimpleName());
                    throw ex;
                }
            }
        }
    }
}
