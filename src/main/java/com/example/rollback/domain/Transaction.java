package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 거래 정보와 상태를 관리하는 엔티티입니다.
 */
@Slf4j
@Data
public class Transaction {
    /** 거래 고유 ID */
    private Long id;
    
    /** 거래 고유 식별자 (GUID) */
    private String guid;
    
    /** 출금 계좌 ID (이체 거래의 경우) */
    private Long fromAccountId;
    
    /** 입금 계좌 ID */
    private Long toAccountId;
    
    /** 거래를 요청한 고객 ID */
    private Long customerId;
    
    /** 거래 유형 (입금, 출금, 수수료 등) */
    private TransactionType transactionType;
    
    /** 거래 금액 */
    private java.math.BigDecimal amount;
    
    /** 통화 코드 */
    private String currency;
    
    /** 거래 설명 또는 메모 */
    private String description;
    
    /** 거래 상태 */
    private TransactionStatus status;
    
    /** 거래 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 거래 완료 일시 */
    private LocalDateTime completedAt;
    
    /** 거래 실패 사유 */
    private String failureReason;

    /**
     * 입금 거래를 생성합니다.
     */
    public static Transaction createDeposit(String guid, Long accountId, Long customerId,
                                          java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = createBaseTransaction(guid, customerId, amount, currency, description, TransactionType.DEPOSIT);
        transaction.toAccountId = accountId;
        
        log.info("입금 거래 생성 - GUID: {}, 계좌: {}, 금액: {}", guid, accountId, amount);
        return transaction;
    }

    /**
     * 출금 거래를 생성합니다.
     */
    public static Transaction createWithdrawal(String guid, Long accountId, Long customerId,
                                                java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = createBaseTransaction(guid, customerId, amount, currency, description, TransactionType.WITHDRAWAL);
        transaction.fromAccountId = accountId;
        
        log.info("출금 거래 생성 - GUID: {}, 계좌: {}, 금액: {}", guid, accountId, amount);
        return transaction;
    }

    /**
     * 거래를 완료 처리합니다.
     */
    public void complete() {
        updateTransactionStatus(TransactionStatus.COMPLETED, null,
                                "거래 완료 - GUID: {}, 유형: {}, 금액: {}", guid, transactionType, amount);
    }

    /**
     * 거래를 실패 처리합니다.
     */
    public void fail(String reason) {
        updateTransactionStatus(TransactionStatus.FAILED, reason,
                                "거래 실패 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
    }

    /**
     * 거래를 취소 처리합니다.
     */
    public void cancel(String reason) {
        updateTransactionStatus(TransactionStatus.CANCELLED, reason,
                                "거래 취소 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
    }

    /**
     * 완료 여부를 반환합니다.
     */
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }
    
    /**
     * 거래 ID를 반환합니다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 실패 여부를 반환합니다.
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }

    /**
     * 공통 필드가 채워진 거래를 생성합니다.
     */
    private static Transaction createBaseTransaction(String guid, Long customerId,
                                                   java.math.BigDecimal amount, String currency,
                                                   String description, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.guid = guid;
        transaction.customerId = customerId;
        transaction.transactionType = transactionType;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.description = description;
        transaction.status = TransactionStatus.PENDING;
        transaction.createdAt = LocalDateTime.now();
        return transaction;
    }

    /**
     * 상태/완료시간/실패사유를 갱신합니다.
     */
    private void updateTransactionStatus(TransactionStatus newStatus, String reason, String logMessage, Object... logArgs) {
        this.status = newStatus;
        this.completedAt = LocalDateTime.now();
        if (reason != null) {
            this.failureReason = reason;
        }
        log.info(logMessage, logArgs);
    }
}
