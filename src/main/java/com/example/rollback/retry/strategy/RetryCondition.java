package com.example.rollback.retry.strategy;

import com.example.rollback.retry.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 재시도 대상 예외인지 판단합니다.
 * (락/데드락/명시적 재시도 예외)
 */
@Slf4j
@Component
public class RetryCondition {

    /**
     * 락 충돌을 식별하는 메시지 키워드 목록입니다.
     */
    private static final List<String> KEYWORDS = List.of(
        "ora-00054",                    // 오라클
        "timeout trying to lock",       // 일반 락 타임아웃
        "lock timeout",                 // 락 타임아웃
        "busy",                         // 리소스 점유
        "lock conflict",                // 락 충돌
        "could not obtain lock"         // 락 획득 실패
    );

    /**
     * 재시도 가능 여부를 판단합니다.
     */
    public boolean isRetryable(Throwable t) {
        if (t == null) {
            log.debug("예외가 null이므로 재시도 불가");
            return false;
        }

        // 데드락
        if (isDeadlockRetryable(t)) {
            return true;
        }

        // 락 충돌
        return isLockRetryable(t);
    }

    /**
     * 데드락 여부를 확인합니다.
     */
    private boolean isDeadlockRetryable(Throwable t) {
        if (t == null || t.getMessage() == null) {
            return false;
        }

        String message = t.getMessage().toLowerCase();
        boolean isDeadlock = message.contains("deadlock");

        if (isDeadlock) {
            log.debug("데드락 키워드 감지 '{}' - 재시도 가능", message);
        }
        return isDeadlock;
    }

    /**
     * 락 충돌 여부를 확인합니다. (원인 예외까지 재귀 탐색)
     */
    private boolean isLockRetryable(Throwable t) {
        if (t == null) {
            return false;
        }

        // 예외 타입
        if (t instanceof PessimisticLockingFailureException) {
            log.debug("PessimisticLockingFailureException 감지 - 재시도 가능");
            return true;
        }

        if (t instanceof RetryableException) {
            log.debug("RetryableException 감지 - 재시도 가능");
            return true;
        }

        // 메시지 키워드
        String msg = (t.getMessage() != null ? t.getMessage() : "").toLowerCase();
        boolean hasKeyword = KEYWORDS.stream().anyMatch(msg::contains);

        if (hasKeyword) {
            log.debug("락 관련 키워드 감지 '{}' - 재시도 가능", msg);
            return true;
        }

        // 원인 예외 재귀 탐색
        boolean causeRetryable = isLockRetryable(t.getCause());
        if (causeRetryable) {
            log.debug("원인 예외에서 락 관련 조건 발견 - 재시도 가능");
        }

        return causeRetryable;
    }
}
