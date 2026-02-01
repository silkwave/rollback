package com.example.rollback.domain;

// 거래 유형 열거형
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    FEE,
    INTEREST,
    PENALTY
}