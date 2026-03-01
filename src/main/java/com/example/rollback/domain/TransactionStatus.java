package com.example.rollback.domain;

/**
 * 거래 처리 상태입니다.
 */
public enum TransactionStatus {
    /**
     * 대기
     */
    PENDING,
    
    /**
     * 완료
     */
    COMPLETED,
    
    /**
     * 실패
     */
    FAILED,
    
    /**
     * 취소
     */
    CANCELLED,
    
    /**
     * 롤백/환불
     */
    REVERSED
}
