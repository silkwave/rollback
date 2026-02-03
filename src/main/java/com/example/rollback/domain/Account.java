package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 은행 계좌 엔티티 클래스
 * 
 * <p>이 클래스는 은행 계좌의 모든 정보를 관리하며, 계좌 생성, 입금, 출금, 동결 등의
 * 핵심적인 계좌 관리 기능을 제공합니다. 계좌 상태, 잔액, 거래 한도 등의 정보를 포함합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>계좌 생성 및 초기화</li>
 *   <li>입금 및 출금 처리</li>
 *   <li>계좌 상태 관리 (활성화, 동결 등)</li>
 *   <li>일일/월간 거래 한도 관리</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Data
public class Account {
    /** 기본 월초계좌 한도 */
    private static final java.math.BigDecimal DEFAULT_OVERDRAFT_LIMIT = new java.math.BigDecimal("1000000");
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
    
    /** 월초계좌 한도 (신용카드, 법인계좌에만 적용) */
    private java.math.BigDecimal overdraftLimit;
    
    /** 계좌 상태 (활성, 동결, 폐쇄, 정지) */
    private AccountStatus status;
    
    /** 계좌 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 계좌 정보 최종 수정 일시 */
    private LocalDateTime updatedAt;
    
    /** 마지막 거래 일시 */
    private LocalDateTime lastTransactionAt;
    
    /** 일일 거래 한도 */
    private java.math.BigDecimal dailyTransactionLimit;
    
    /** 월간 거래 한도 */
    private java.math.BigDecimal monthlyTransactionLimit;
    
    /** 당일 누적 거래 금액 */
    private java.math.BigDecimal dailyTransactionAmount;
    
    /** 당월 누적 거래 금액 */
    private java.math.BigDecimal monthlyTransactionAmount;
    
    /** 일일 한도 마지막 초기화 일시 */
    private LocalDateTime lastDailyReset;
    
    /** 월간 한도 마지막 초기화 일시 */
    private LocalDateTime lastMonthlyReset;
    
    /** 계좌주 성명 */
    private String accountHolderName;
    
    /** 지점 코드 */
    private String branchCode;

    /**
     * 새로운 계좌를 생성하는 팩토리 메서드
     * 
     * @param accountNumber 계좌번호 (중복되지 않아야 함)
     * @param customerId 계좌 소유자 고객 ID
     * @param accountType 계좌 유형 (CHECKING, SAVINGS, CREDIT, BUSINESS)
     * @param currency 통화 코드 (KRW, USD, EUR 등)
     * @param initialDeposit 초기 입금액 (null 가능)
     * @param accountHolderName 계좌주 성명
     * @return 생성된 Account 객체
     */
    public static Account create(String accountNumber, Long customerId, AccountType accountType, 
                                String currency, java.math.BigDecimal initialDeposit, String accountHolderName) {
        Account account = new Account();
        account.accountNumber = accountNumber;
        account.customerId = customerId;
        account.accountType = accountType;
        account.currency = currency;
        account.balance = initialDeposit != null ? initialDeposit : java.math.BigDecimal.ZERO;
        account.overdraftLimit = accountType == AccountType.CREDIT || accountType == AccountType.BUSINESS ? 
            DEFAULT_OVERDRAFT_LIMIT : java.math.BigDecimal.ZERO;
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
        
        account.logAccountActivity("생성됨 - 고객ID: {}, 유형: {}, 초기잔액: {}", customerId, accountType, account.balance);
        return account;
    }

    /**
     * 계좌에 입금을 처리합니다
     * 
     * @param amount 입금할 금액 (0보다 커야 함)
     * @return 입금이 완료된 Account 객체 (메서드 체이닝을 위함)
     * @throws IllegalArgumentException 금액이 0 이하일 경우
     */
    public void deposit(java.math.BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("입금 완료 - 금액: {}, 신규잔액: {}", amount, balance);
    }

    /**
     * 계좌에서 출금을 처리합니다
     * 
     * @param amount 출금할 금액 (0보다 커야 함)
     * @return 출금이 완료된 Account 객체 (메서드 체이닝을 위함)
     * @throws IllegalArgumentException 금액이 0 이하일 경우
     * @throws IllegalStateException 잔액이 부족할 경우
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
     * 계좌를 동결 상태로 변경합니다
     * 
     * <p>동결된 계좌는 입출금이 제한됩니다. 보안 상의 이유나
     * 법적 요청으로 인해 계좌를 임시 정지할 때 사용합니다.</p>
     * 
     * @return 동결된 Account 객체
     */
    public void freeze() {
        this.status = AccountStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("동결");
    }

    /**
     * 동결된 계좌를 활성 상태로 변경합니다
     * 
     * <p>동결 또는 정지 상태였던 계좌를 다시 정상적인 입출금이
     * 가능한 상태로 되돌립니다.</p>
     * 
     * @return 활성화된 Account 객체
     */
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        this.logAccountActivity("활성화");
    }

    /**
     * 계좌에 충분한 잔액이 있는지 확인합니다
     * 
     * <p>월초계좌 한도를 포함하여 출금 가능한지 판단합니다.
     * 현재 잔액 + 월초계좌 한도 >= 요청 금액</p>
     * 
     * @param amount 확인할 금액
     * @return 출금 가능하면 true, 불가능하면 false
     */
    public boolean hasSufficientFunds(java.math.BigDecimal amount) {
        return balance.add(overdraftLimit).compareTo(amount) >= 0;
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
     * @param message 포맷팅된 로그 메시지.
     * @param args 메시지에 포함될 인자들.
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