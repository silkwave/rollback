package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 계좌 엔티티입니다. (잔액/상태/거래일시 관리)
 */
@Slf4j
@Data
public class Account {
    /** 계좌 고유 ID */
    private Long id;
    
    /** 계좌번호 (중복되지 않는 고유 값) */
    private String accountNumber;
    
    /** 계좌 소유자 고객 ID */
    private Long customerId;
    
    /** 계좌 유형 (예금, 적금, 신용, 법인) */
    private AccountType accountType;
    
    /** 통화 코드 (KRW, USD, EUR 등) */
    private String currency;
    
    /** 현재 계좌 잔액 */
    private java.math.BigDecimal balance;
    
    /** 계좌 상태 (활성, 동결, 폐쇄, 정지) */
    private AccountStatus status;
    
    /** 계좌 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 계좌 정보 최종 수정 일시 */
    private LocalDateTime updatedAt;
    
    /** 마지막 거래 일시 */
    private LocalDateTime lastTransactionAt;
    
    /** 계좌주 성명 */
    private String accountHolderName;

    /**
     * 계좌를 생성합니다.
     */
    public static Account create(String accountNumber, Long customerId, AccountType accountType, 
                                String currency, java.math.BigDecimal initialDeposit, String accountHolderName) {
        Account account = new Account();
        account.accountNumber = accountNumber;
        account.customerId = customerId;
        account.accountType = accountType;
        account.currency = currency;
        account.balance = initialDeposit != null ? initialDeposit : java.math.BigDecimal.ZERO;
        account.status = AccountStatus.ACTIVE;
        account.createdAt = LocalDateTime.now();
        account.updatedAt = LocalDateTime.now();
        account.lastTransactionAt = LocalDateTime.now();
        
        account.accountHolderName = accountHolderName;
        
        account.logAccountActivity("생성됨 - 고객ID: {}, 유형: {}, 초기잔액: {}", customerId, accountType, account.balance);
        return account;
    }

    /**
     * 입금합니다.
     */
    public void deposit(java.math.BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("입금 완료 - 금액: {}, 신규잔액: {}", amount, balance);
    }

    /**
     * 출금합니다.
     */
    public void withdraw(java.math.BigDecimal amount) {
        validateAmount(amount);
        validateSufficientFunds(amount);
        this.balance = this.balance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("출금 완료 - 금액: {}, 신규잔액: {}", amount, balance);
    }

    /**
     * 계좌를 동결합니다.
     */
    public void freeze() {
        this.status = AccountStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("동결");
    }

    /**
     * 계좌를 활성화합니다.
     */
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("활성화");
    }

    /**
     * 출금 가능 여부(잔액 >= 금액)를 확인합니다.
     */
    public boolean hasSufficientFunds(java.math.BigDecimal amount) {
        return balance.compareTo(amount) >= 0; // 잔액 비교
    }

    /**
     * 계좌가 활성 상태인지 확인합니다
     * 
     * @return 활성 상태이면 true, 아니면 false
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * 계좌번호를 반환합니다
     * 
     * @return 계좌번호
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * 계좌 관련 활동을 로깅하는 유틸리티 메서드.
     * @param message 로그 템플릿
     * @param args 템플릿 인자
     */
    private void logAccountActivity(String message, Object... args) {
        log.info("계좌번호: {} - " + message, this.accountNumber, args);
    }

    /**
     * 금액의 유효성을 검사합니다
     * 
     * @param amount 검사할 금액
     * @throws IllegalArgumentException 금액이 null이거나 0 이하일 경우
     */
    private void validateAmount(java.math.BigDecimal amount) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
    }

    /**
     * 잔액이 충분한지 검사합니다
     * 
     * @param amount 출금할 금액
     * @throws IllegalStateException 잔액이 부족할 경우
     */
    private void validateSufficientFunds(java.math.BigDecimal amount) {
        if (!hasSufficientFunds(amount)) {
            throw new IllegalStateException(
                String.format("잔액이 부족합니다. 잔액: %s, 출금요청: %s", balance, amount));
        }
    }
}
