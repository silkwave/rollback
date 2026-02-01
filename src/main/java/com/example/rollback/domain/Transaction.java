package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// 거래 내역 엔티티
@Slf4j
@Data
public class Transaction {
    private Long id;
    private String guid;
    private Long fromAccountId;
    private Long toAccountId;
    private Long customerId;
    private TransactionType transactionType;
    private java.math.BigDecimal amount;
    private String currency;
    private String description;
    private TransactionStatus status;
    private String referenceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;
    
    // 은행 거래 추가 필드
    private String ipAddress;
    private String deviceInfo;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String transactionChannel;
    private String transactionCategory;
    private java.math.BigDecimal feeAmount;
    private java.math.BigDecimal balanceAfter;

    // 입금 거래 생성
    public static Transaction createDeposit(String guid, Long accountId, Long customerId,
                                          java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = new Transaction();
        transaction.guid = guid;
        transaction.toAccountId = accountId;
        transaction.customerId = customerId;
        transaction.transactionType = TransactionType.DEPOSIT;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.status = TransactionStatus.PENDING;
        transaction.transactionChannel = "ONLINE";
        transaction.feeAmount = java.math.BigDecimal.ZERO;
        transaction.createdAt = LocalDateTime.now();
        
        log.info("입금 거래 생성 - GUID: {}, 계좌: {}, 금액: {}", guid, accountId, amount);
        return transaction;
    }

    // 출금 거래 생성
    public static Transaction createWithdrawal(String guid, Long accountId, Long customerId,
                                              java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = new Transaction();
        transaction.guid = guid;
        transaction.fromAccountId = accountId;
        transaction.customerId = customerId;
        transaction.transactionType = TransactionType.WITHDRAWAL;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.status = TransactionStatus.PENDING;
        transaction.transactionChannel = "ONLINE";
        transaction.feeAmount = java.math.BigDecimal.ZERO;
        transaction.createdAt = LocalDateTime.now();
        
        log.info("출금 거래 생성 - GUID: {}, 계좌: {}, 금액: {}", guid, accountId, amount);
        return transaction;
    }

    // 이체 거래 생성
    public static Transaction createTransfer(String guid, Long fromAccountId, Long toAccountId, Long customerId,
                                           java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = new Transaction();
        transaction.guid = guid;
        transaction.fromAccountId = fromAccountId;
        transaction.toAccountId = toAccountId;
        transaction.customerId = customerId;
        transaction.transactionType = TransactionType.TRANSFER;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.status = TransactionStatus.PENDING;
        transaction.transactionChannel = "ONLINE";
        transaction.feeAmount = java.math.BigDecimal.ZERO;
        transaction.createdAt = LocalDateTime.now();
        
        log.info("이체 거래 생성 - GUID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}", 
            guid, fromAccountId, toAccountId, amount);
        return transaction;
    }

    // 거래 완료 처리
    public Transaction complete() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        log.info("거래 완료 - GUID: {}, 유형: {}, 금액: {}", guid, transactionType, amount);
        return this;
    }

    // 거래 실패 처리
    public Transaction fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
        log.info("거래 실패 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
        return this;
    }

    // 거래 취소 처리
    public Transaction cancel(String reason) {
        this.status = TransactionStatus.CANCELLED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
        log.info("거래 취소 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
        return this;
    }

    // 참조번호 생성
    public String generateReferenceNumber() {
        if (this.referenceNumber == null) {
            this.referenceNumber = "TXN" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        }
        return this.referenceNumber;
    }

    // 완료된 거래 확인
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }
    
    // ID getter
    public Long getId() {
        return id;
    }

    // 실패한 거래 확인
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }

    // 대기 중인 거래 확인
    public boolean isPending() {
        return TransactionStatus.PENDING.equals(this.status);
    }

    // 이체 거래 확인
    public boolean isTransfer() {
        return TransactionType.TRANSFER.equals(this.transactionType);
    }
}