package com.example.rollback.domain;

/**
 * 거래 유형입니다.
 */
public enum TransactionType {
    /**
     * 입금
     */
    DEPOSIT,
    
    /**
     * 출금
     */
    WITHDRAWAL,

    /**
     * 수수료
     */
    FEE,
    
    /**
     * 이자
     */
    INTEREST,
    
    /**
     * 벌금/위약금
     */
    PENALTY
}
