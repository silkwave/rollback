package com.example.rollback.retry.strategy;

import org.springframework.stereotype.Component;

/** 데드락 여부를 판단하는 조건 */
@Component
public class DeadlockRetryCondition implements RetryCondition {
    @Override
    public boolean isRetryable(Throwable t) {
        return t != null && t.getMessage() != null && 
               t.getMessage().toLowerCase().contains("deadlock");
    }
}
