package com.example.rollback.domain;

/**
 * 계좌 유형입니다.
 */
public enum AccountType {
    /**
     * 보통예금(입출금 자유)
     */
    CHECKING,
    
    /**
     * 저축예금(적립/제한 가능)
     */
    SAVINGS,
    
    /**
     * 신용계좌(한도 기반)
     */
    CREDIT,
    
    /**
     * 법인계좌
     */
    BUSINESS
}
