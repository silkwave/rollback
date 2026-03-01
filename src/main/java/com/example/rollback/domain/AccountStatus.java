package com.example.rollback.domain;

/**
 * 계좌 상태입니다.
 */
public enum AccountStatus {
    /**
     * 정상(입출금 가능)
     */
    ACTIVE,
    
    /**
     * 동결(거래 중지)
     */
    FROZEN,
    
    /**
     * 폐쇄(종료)
     */
    CLOSED,
    
    /**
     * 정지(관리자 제한)
     */
    SUSPENDED
}
