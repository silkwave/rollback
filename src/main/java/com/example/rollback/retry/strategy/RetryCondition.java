package com.example.rollback.retry.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 재시도 가능 여부를 판단하는 조건 클래스
 *
 * <p>이 클래스는 데이터베이스 락 충돌 및 데드락과 같은 특정 예외 상황에서
 * 트랜잭션 재시도 가능 여부를 판단합니다. 기존의 LockRetryCondition과
 * DeadlockRetryCondition의 기능을 통합하여 단일 클래스에서 관리합니다.</p>
 *
 * <p><strong>주요 판단 기준:</strong></p>
 * <ul>
 *   <li><strong>예외 타입:</strong> Spring의 PessimisticLockingFailureException</li>
 *   <li><strong>메시지 키워드:</strong> "deadlock" 및 다양한 데이터베이스의 락 관련 에러 메시지</li>
 *   <li><strong>원인 예외:</strong> 체이닝된 예외의 원인까지 재귀적으로 탐색</li>
 * </ul>
 *
 * <p><strong>지원하는 데이터베이스:</strong></p>
 * <ul>
 *   <li>MySQL, Oracle, PostgreSQL, SQL Server 등 다양한 DB의 락/데드락 에러</li>
 *   <li>일반적인 JPA/Hibernate 락 충돌</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Spring에서 자동 주입
 * @Autowired private RetryCondition retryCondition;
 *
 * // 직접 사용
 * if (retryCondition.isRetryable(exception)) {
 *     // 재시도 로직 실행
 * }
 * }</pre>
 *
 * @author Rollback Team
 * @since 1.0
 * @see com.example.rollback.retry.strategy.RandomBackoffRetryStrategy
 */
@Slf4j
@Component
public class RetryCondition {

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
        "could not obtain lock"         // 락 획득 실패
    );

    /**
     * 주어진 예외가 재시도 가능한 조건인지 판단합니다.
     * LockRetryCondition과 DeadlockRetryCondition의 로직을 통합합니다.
     *
     * @param t 판단할 예외 객체 (null 가능)
     * @return 재시도 가능하면 true, 그렇지 않으면 false
     */
    public boolean isRetryable(Throwable t) {
        if (t == null) {
            log.debug("예외가 null이므로 재시도 불가");
            return false;
        }

        // 데드락 조건 확인
        if (isDeadlockRetryable(t)) {
            return true;
        }

        // 락 충돌 조건 확인
        return isLockRetryable(t);
    }

    /**
     * 주어진 예외가 데드락으로 인한 재시도 가능한 예외인지 판단합니다.
     *
     * @param t 판단할 예외 객체 (null 가능)
     * @return 데드락으로 인한 재시도 가능한 예외면 true, 그렇지 않으면 false
     */
    private boolean isDeadlockRetryable(Throwable t) {
        // null 체크
        if (t == null || t.getMessage() == null) {
            return false;
        }

        // 메시지에 "deadlock" 키워드 포함 여부 확인 (대소문자 무관)
        String message = t.getMessage().toLowerCase();
        boolean isDeadlock = message.contains("deadlock");

        if (isDeadlock) {
            log.debug("데드락 키워드 감지 '{}' - 재시도 가능", message);
        }
        return isDeadlock;
    }

    /**
     * 주어진 예외가 락 충돌로 인한 재시도 가능한 예외인지 판단합니다.
     *
     * @param t 판단할 예외 객체 (null 가능)
     * @return 락 충돌로 인한 재시도 가능한 예외면 true, 그렇지 않으면 false
     */
    private boolean isLockRetryable(Throwable t) {
        if (t == null) {
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
        boolean causeRetryable = isLockRetryable(t.getCause());
        if (causeRetryable) {
            log.debug("원인 예외에서 락 관련 조건 발견 - 재시도 가능");
        }

        return causeRetryable;
    }
}
