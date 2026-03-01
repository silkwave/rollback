package com.example.rollback.domain;

/**
 * 고객 상태입니다.
 */
public enum CustomerStatus {
    /**
     * 활성
     */
    ACTIVE,
    
    /**
     * 비활성
     */
    INACTIVE,
    
    /**
     * 정지
     */
    SUSPENDED,
    
    /**
     * 폐쇄
     */
    CLOSED
}
