package com.example.rollback.config;

import com.example.rollback.retry.LockRetryTemplate;
import com.example.rollback.retry.RetryStrategy;
import com.example.rollback.retry.strategy.RandomBackoffRetryStrategy;
import com.example.rollback.retry.strategy.RetryCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager; // PlatformTransactionManager import 추가

/**
 * 재시도(Retry) 관련 기능을 위한 Spring 설정 클래스
 * 
 * <p>이 클래스는 애플리케이션의 재시도 로직을 구성하는 핵심 빈들을 정의합니다.
 * 분산 환경에서 동시성 제어와 장애 복구를 위해 재시도 전략과
 * 분산 락 처리 템플릿을 설정합니다.</p>
 * 
 * <p>주요 구성 요소:</p>
 * <ul>
 *   <li>랜덤 백오프 재시도 전략 설정</li>
 *   <li>분산 락 재시도 템플릿 설정</li>
 *   <li>재시도 조건 컴포넌트 자동 주입</li>
 * </ul>
 * 
 * @author Spring Application Team
 * @version 1.0
 * @since 2024.01.01
 */
@Configuration
public class RetryConfig {

    /**
     * 재시도 전략 빈을 생성하고 설정합니다.
     * 
     * <p>이 메서드는 랜덤 백오프(Random Backoff) 전략을 사용하여
     * 다음과 같은 재시도 정책을 구성합니다:</p>
     * <ul>
     *   <li><strong>최대 재시도 횟수:</strong> 10회 - 임시적 장애에 대한 충분한 복구 기회 제공</li>
     *   <li><strong>기본 대기 시간:</strong> 100ms - 첫 재시도의 기본 지연 시간</li>
     *   <li><strong>최대 지터(Jitter):</strong> 200ms - 동시 재시도를 분산시키기 위한 무작위 지연</li>
     *   <li><strong>최대 대기 시간:</strong> 2000ms - 재시도 간격의 상한선 설정</li>
     * </ul>
     * 
     * <p>랜덤 백오프 전략은 여러 노드가 동시에 재시도할 때
     * 발생하는 '썬더링 허드' 현상을 방지하고 시스템 안정성을 향상시킵니다.</p>
     * 
     * @param retryCondition 재시도 조건 객체. LockRetryCondition과 DeadlockRetryCondition의 기능이 통합되어 있습니다.
     * @return 설정된 랜덤 백오프 재시도 전략 객체
     */
    @Bean
    public RetryStrategy retryStrategy(RetryCondition retryCondition) {
        // 10번 재시도, 기본 100ms 대기, 최대 200ms 지터, 최대 2000ms 대기
        return new RandomBackoffRetryStrategy(10, 100, 200, 2000, retryCondition);
    }

    /**
     * 분산 락 처리를 위한 재시도 템플릿 빈을 생성합니다.
     * 
     * <p>이 템플릿은 분산 환경에서의 경쟁 상태(Race Condition)을
     * 방지하기 위해 다음과 같은 기능을 제공합니다:</p>
     * <ul>
     *   <li><strong>분산 락 획득 및 해제:</strong> 여러 노드 간의 자원 접근 동기화</li>
     *   <li><strong>자동 재시도:</strong> 락 획득 실패 시 설정된 전략에 따른 자동 재시도</li>
     *   <li><strong>예외 처리:</strong> 락 관련 예외의 중앙 처리 및 로깅</li>
     *   <li><strong>타임아웃 관리:</strong> 데드락 방지를 위한 락 보유 시간 제한</li>
     * </ul>
     * 
     * <p>이 템플릿을 통해 복잡한 분산 락 로직을 캡슐화하고
     * 비즈니스 로직에서는 간단한 인터페이스로 락 기능을 사용할 수 있습니다.</p>
     * 
     * @param retryStrategy 앞서 정의된 재시도 전략 빈.
     *                     락 획득 실패 시의 재시도 로직에 사용됩니다.
     * @param transactionManager 트랜잭션 관리자. 재시도 시 새로운 트랜잭션을 시작하는 데 사용됩니다.
     * @return 설정된 분산 락 재시도 템플릿 객체
     */
    @Bean
    public LockRetryTemplate lockRetryTemplate(RetryStrategy retryStrategy, PlatformTransactionManager transactionManager) {
        return new LockRetryTemplate(retryStrategy, transactionManager);
    }
}