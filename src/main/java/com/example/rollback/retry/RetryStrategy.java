package com.example.rollback.retry;

public interface RetryStrategy {
    boolean shouldRetry(Exception e, int attemptCount);
    long getWaitTime(int attemptCount);
}