package com.example.rollback.retry.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 데이터베이스 락 충돌 여부를 판단하는 조건 클래스
 * 
 * <p>이 클래스는 RetryCondition 인터페이스를 구현하여, 다양한 데이터베이스 락 관련
 * 예외들을 식별하고 재시도 가능 여부를 판단합니다. 컴포지트 패턴의 일부로서 개별적인 
 * 재시도 조건을 담당합니다.</p>
 * 
 * <p><strong>주요 판단 기준:</strong></p>
 * <ul>
 *   <li><strong>예외 타입:</strong> Spring의 PessimisticLockingFailureException</li>
 *   <li><strong>메시지 키워드:</strong> 다양한 데이터베이스의 락 관련 에러 메시지</li>
 *   <li><strong>원인 예외:</strong> 체이닝된 예우의 원인까지 재귀적으로 탐색</li>
 * </ul>
 * 
 * <p><strong>지원하는 데이터베이스:</strong></p>
 * <ul>
 *   <li>Oracle: ORA-00054 (resource busy and acquire with NOWAIT specified)</li>
 *   <li>MySQL: lock timeout, deadlock 등</li>
 *   <li>PostgreSQL: lock timeout 등</li>
 *   <li>일반적인 JPA/Hibernate 락 충돌</li>
 * </ul>
 * 
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Spring에서 자동 주입
 * @Autowired private LockRetryCondition lockCondition;
 * 
 * // 직접 사용
 * if (lockCondition.isRetryable(exception)) {
 *     // 재시도 로직 실행
 * }
 * }</pre>
 * 
 * @author Rollback Team
 * @since 1.0
 * @see RetryCondition 재시도 조건 인터페이스
 * @see DeadlockRetryCondition 데드락 조건 클래스
 */
@Slf4j
@Component
public class LockRetryCondition implements RetryCondition {
    
    /**
     * 락 충돌을 식별하는 키워드 목록
     * 다양한 데이터베이스의 락 관련 에러 메시지 키워드를 포함합니다.
     */
    private static final List<String> KEYWORDS = List.of(
        "ora-00054",                    // Oracle 락 타임아웃
        "timeout trying to lock",       // 일반적인 락 타임아웃
        "lock timeout",                 // 락 타임아웃
        "busy",                         // 리소스 사용 중
        "lock conflict",                // 락 충돌
        "could not obtain lock",        // 락 획득 실패
        "deadlock detected"             // 데드락 감지 (일부 DB)
    );

    /**
     * 주어진 예외가 락 충돌로 인한 재시도 가능한 예외인지 판단합니다.
     * 
     * <p>판단 로직은 다음과 같은 순서로 진행됩니다:</p>
     * <ol>
     *   <li>null 체크 - 예외가 null이면 재시도 불가</li>
     *   <li>특정 예외 타입 확인 - PessimisticLockingFailureException이면 재시도 가능</li>
     *   <li>메시지 키워드 확인 - 예외 메시지에 락 관련 키워드 포함 시 재시도 가능</li>
     *   <li>원인 예외 재귀 탐색 - 체이닝된 예외의 원인까지 동일한 로직으로 판단</li>
     * </ol>
     * 
     * @param t 판단할 예외 객체 (null 가능)
     * @return 락 충돌로 인한 재시도 가능한 예외면 true, 그렇지 않으면 false
     */
    @Override
    public boolean isRetryable(Throwable t) {
        if (t == null) {
            log.debug("예외가 null이므로 재시도 불가");
            return false;
        }
        
        // 1. 특정 예외 타입 확인
        if (t instanceof PessimisticLockingFailureException) {
            log.debug("PessimisticLockingFailureException 감지 - 재시도 가능");
            return true;
        }
        
        // 2. 메시지 키워드 확인 (재귀적으로 Cause 탐색)
        String msg = (t.getMessage() != null ? t.getMessage() : "").toLowerCase();
        boolean hasKeyword = KEYWORDS.stream().anyMatch(msg::contains);
        
        if (hasKeyword) {
            log.debug("락 관련 키워드 감지 '{}' - 재시도 가능", msg);
            return true;
        }
        
        // 3. 원인 예외 재귀 탐색
        boolean causeRetryable = isRetryable(t.getCause());
        if (causeRetryable) {
            log.debug("원인 예외에서 락 관련 조건 발견 - 재시도 가능");
        }
        
        return causeRetryable;
    }
}
