package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// 은행 계좌 엔티티
@Slf4j
@Data
public class Account {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private AccountType accountType;
    private String currency; // KRW, USD, EUR
    private java.math.BigDecimal balance;
    private java.math.BigDecimal overdraftLimit;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTransactionAt;
    
    // 은행 시스템 추가 필드
    private java.math.BigDecimal dailyTransactionLimit;
    private java.math.BigDecimal monthlyTransactionLimit;
    private java.math.BigDecimal dailyTransactionAmount;
    private java.math.BigDecimal monthlyTransactionAmount;
    private LocalDateTime lastDailyReset;
    private LocalDateTime lastMonthlyReset;
    private String accountHolderName;
    private String branchCode;

    // 계좌 생성 팩토리 메서드
    public static Account create(String accountNumber, Long customerId, AccountType accountType, 
                                String currency, java.math.BigDecimal initialDeposit, String accountHolderName) {
        Account account = new Account();
        account.accountNumber = accountNumber;
        account.customerId = customerId;
        account.accountType = accountType;
        account.currency = currency;
        account.balance = initialDeposit != null ? initialDeposit : java.math.BigDecimal.ZERO;
        account.overdraftLimit = accountType == AccountType.CREDIT || accountType == AccountType.BUSINESS ? 
            new java.math.BigDecimal("1000000") : java.math.BigDecimal.ZERO;
        account.status = AccountStatus.ACTIVE;
        account.createdAt = LocalDateTime.now();
        account.updatedAt = LocalDateTime.now();
        account.lastTransactionAt = LocalDateTime.now();
        
        // 은행 시스템 기본값 설정
        account.accountHolderName = accountHolderName;
        account.dailyTransactionLimit = new java.math.BigDecimal("1000000");
        account.monthlyTransactionLimit = new java.math.BigDecimal("5000000");
        account.dailyTransactionAmount = java.math.BigDecimal.ZERO;
        account.monthlyTransactionAmount = java.math.BigDecimal.ZERO;
        account.lastDailyReset = LocalDateTime.now();
        account.lastMonthlyReset = LocalDateTime.now();
        
        log.info("계좌 생성됨 - 계좌번호: {}, 고객ID: {}, 유형: {}, 초기잔액: {}", 
            accountNumber, customerId, accountType, account.balance);
        return account;
    }

    // 입금 처리
    public Account deposit(java.math.BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        log.info("입금 완료 - 계좌번호: {}, 금액: {}, 신규잔액: {}", 
            accountNumber, amount, balance);
        return this;
    }

    // 출금 처리
    public Account withdraw(java.math.BigDecimal amount) {
        validateAmount(amount);
        validateSufficientFunds(amount);
        this.balance = this.balance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        log.info("출금 완료 - 계좌번호: {}, 금액: {}, 신규잔액: {}", 
            accountNumber, amount, balance);
        return this;
    }

    // 계좌 동결
    public Account freeze() {
        this.status = AccountStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
        log.info("계좌 동결 - 계좌번호: {}", accountNumber);
        return this;
    }

    // 계좌 활성화
    public Account activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        log.info("계좌 활성화 - 계좌번호: {}", accountNumber);
        return this;
    }

    // 계좌 잔액 확인
    public boolean hasSufficientFunds(java.math.BigDecimal amount) {
        return balance.add(overdraftLimit).compareTo(amount) >= 0;
    }

    // 활성 계좌 확인
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }
    
    // 계좌번호 getter
    public String getAccountNumber() {
        return accountNumber;
    }

    // 금액 유효성 검사
    private void validateAmount(java.math.BigDecimal amount) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
    }

    // 잔액 충분성 검사
    private void validateSufficientFunds(java.math.BigDecimal amount) {
        if (!hasSufficientFunds(amount)) {
            throw new IllegalStateException(
                String.format("잔액이 부족합니다. 잔액: %s, 출금요청: %s", balance, amount));
        }
    }


}