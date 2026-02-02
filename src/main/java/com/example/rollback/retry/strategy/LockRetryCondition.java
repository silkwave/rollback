package com.example.rollback.retry.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;
import java.util.List;

/** 락 충돌 여부를 판단하는 조건 */
@Slf4j
@Component
public class LockRetryCondition implements RetryCondition {
    private static final List<String> KEYWORDS = List.of(
        "ora-00054", "timeout trying to lock", "lock timeout", "busy", "lock conflict"
    );

    @Override
    public boolean isRetryable(Throwable t) {
        if (t == null) return false;
        // 1. 특정 예외 타입 확인
        if (t instanceof PessimisticLockingFailureException) return true;
        
        // 2. 메시지 키워드 확인 (재귀적으로 Cause 탐색)
        String msg = (t.getMessage() != null ? t.getMessage() : "").toLowerCase();
        return KEYWORDS.stream().anyMatch(msg::contains) || isRetryable(t.getCause());
    }
}
