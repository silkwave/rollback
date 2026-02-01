package com.example.rollback.domain;

// 거래 유형 열거형
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,

    FEE,
    INTEREST,
    PENALTY
}