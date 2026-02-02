package com.example.rollback.retry.strategy;

import org.springframework.stereotype.Component;

/**
 * 데이터베이스 데드락 여부를 판단하는 조건 클래스
 * 
 * <p>이 클래스는 RetryCondition 인터페이스를 구현하여, 데이터베이스 데드락으로
 * 인한 예외를 식별하고 재시도 가능 여부를 판단합니다. 컴포지트 패턴의 일부로서 
 * 개별적인 재시도 조건을 담당합니다.</p>
 * 
 * <p><strong>데드락이란?</strong></p>
 * <p>두 개 이상의 트랜잭션이 서로가 가진 리소스를 기다리면서 무한히 대기하는
 * 상황을 말합니다. 데드락은 일시적인 현상이므로, 트랜잭션을 롤백하고 다시 시도하면
 * 대부분 해결될 수 있습니다.</p>
 * 
 * <p><strong>주요 특징:</strong></p>
 * <ul>
 *   <li><strong>단순성:</strong> 예외 메시지에 "deadlock" 키워드 포함 여부로 판단</li>
 *   <li><strong>범용성:</strong> 대부분의 데이터베이스에서 발생하는 데드락 에러 대응</li>
 *   <li><strong>신뢰성:</strong> 데드락은 재시도 시 해결될 확률이 높음</li>
 * </ul>
 * 
 * <p><strong>지원하는 데이터베이스:</strong></p>
 * <ul>
 *   <li>MySQL: "Deadlock found when trying to get lock"</li>
 *   <li>Oracle: "ORA-00060: deadlock detected"</li>
 *   <li>PostgreSQL: "deadlock detected"</li>
 *   <li>SQL Server: "Deadlock encountered"</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Spring에서 자동 주입
 * @Autowired private DeadlockRetryCondition deadlockCondition;
 * 
 * // 직접 사용
 * if (deadlockCondition.isRetryable(exception)) {
 *     // 데드락이므로 재시도 로직 실행
 * }
 * }</pre>
 * 
 * @author Rollback Team
 * @since 1.0
 * @see RetryCondition 재시도 조건 인터페이스
 * @see LockRetryCondition 락 충돌 조건 클래스
 */
@Component
public class DeadlockRetryCondition implements RetryCondition {
    
    /**
     * 주어진 예외가 데드락으로 인한 재시도 가능한 예외인지 판단합니다.
     * 
     * <p>판단 로직은 매우 단순하며, 예외 메시지에 "deadlock" 키워드가 포함된
     * 경우에만 재시도 가능하다고 판단합니다. 이는 대부분의 데이터베이스가 
     * 데드락 발생 시 메시지에 이 키워드를 포함하기 때문입니다.</p>
     * 
     * @param t 판단할 예외 객체 (null 가능)
     * @return 데드락으로 인한 재시도 가능한 예외면 true, 그렇지 않으면 false
     */
    @Override
    public boolean isRetryable(Throwable t) {
        // null 체크
        if (t == null || t.getMessage() == null) {
            return false;
        }
        
        // 메시지에 "deadlock" 키워드 포함 여부 확인 (대소문자 무관)
        String message = t.getMessage().toLowerCase();
        boolean isDeadlock = message.contains("deadlock");
        
        return isDeadlock;
    }
}
