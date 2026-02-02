package com.example.rollback.retry.strategy;

/** 재시도 가능 여부를 판단하는 조건 인터페이스 */
public interface RetryCondition {
    boolean isRetryable(Throwable t);
}
