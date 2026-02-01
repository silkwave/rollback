package com.example.rollback.domain;

// 거래 상태 열거형
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED
}