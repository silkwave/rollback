package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 거래 내역 엔티티 클래스
 * 
 * <p>이 클래스는 모든 금융 거래의 정보를 관리하며, 입금, 출금, 이체, 수수료 등
 * 다양한 유형의 거래를 처리합니다. 거래 생성, 상태 관리, 완료/실패 처리 등의 기능을 제공합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>입금/출금 거래 생성</li>
 *   <li>거래 상태 관리</li>
 *   <li>거래 완료/실패/취소 처리</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024-01-01
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
     * 입금 거래를 생성하는 팩토리 메서드
     * 
     * @param guid 거래 고유 식별자
     * @param accountId 입금받을 계좌 ID
     * @param customerId 거래를 요청한 고객 ID
     * @param amount 입금 금액
     * @param currency 통화 코드
     * @param description 거래 설명
     * @return 생성된 Transaction 객체
     */
    public static Transaction createDeposit(String guid, Long accountId, Long customerId,
                                          java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = createBaseTransaction(guid, customerId, amount, currency, description, TransactionType.DEPOSIT);
        transaction.toAccountId = accountId;
        
        log.info("입금 거래 생성 - GUID: {}, 계좌: {}, 금액: {}", guid, accountId, amount);
        return transaction;
    }

    /**
     * 출금 거래를 생성하는 팩토리 메서드
     * 
     * @param guid 거래 고유 식별자
     * @param accountId 출금할 계좌 ID
     * @param customerId 거래를 요청한 고객 ID
     * @param amount 출금 금액
     * @param currency 통화 코드
     * @param description 거래 설명
     * @return 생성된 Transaction 객체
     */
    public static Transaction createWithdrawal(String guid, Long accountId, Long customerId,
                                                java.math.BigDecimal amount, String currency, String description) {
        Transaction transaction = createBaseTransaction(guid, customerId, amount, currency, description, TransactionType.WITHDRAWAL);
        transaction.fromAccountId = accountId;
        
        log.info("출금 거래 생성 - GUID: {}, 계좌: {}, 금액: {}", guid, accountId, amount);
        return transaction;
    }

    /**
     * 거래를 성공적으로 완료 처리합니다
     * 
     * <p>거래 상태를 COMPLETED로 변경하고 완료 시간을 기록합니다.</p>
     */
    public void complete() {
        updateTransactionStatus(TransactionStatus.COMPLETED, null,
                                "거래 완료 - GUID: {}, 유형: {}, 금액: {}", guid, transactionType, amount);
    }

    /**
     * 거래를 실패 처리합니다
     * 
     * @param reason 실패 사유
     */
    public void fail(String reason) {
        updateTransactionStatus(TransactionStatus.FAILED, reason,
                                "거래 실패 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
    }

    /**
     * 거래를 취소 처리합니다
     * 
     * @param reason 취소 사유
     */
    public void cancel(String reason) {
        updateTransactionStatus(TransactionStatus.CANCELLED, reason,
                                "거래 취소 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
    }

    /**
     * 거래가 완료된 상태인지 확인합니다
     * 
     * @return 완료된 거래이면 true, 아니면 false
     */
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }
    
    /**
     * 거래 ID를 반환합니다
     * 
     * @return 거래 고유 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 거래가 실패한 상태인지 확인합니다
     * 
     * @return 실패한 거래이면 true, 아니면 false
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }

    /**
     * 모든 거래 유형에 공통적인 기본 거래 객체를 생성합니다.
     * @param guid 거래 고유 식별자
     * @param customerId 거래를 요청한 고객 ID
     * @param amount 거래 금액
     * @param currency 통화 코드
     * @param description 거래 설명
     * @param transactionType 거래 유형
     * @return 기본 필드가 초기화된 Transaction 객체
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
     * 거래의 상태를 업데이트하고 완료 시간을 기록하며, 필요에 따라 실패 사유를 설정합니다.
     * @param newStatus 새로운 거래 상태
     * @param reason 실패 또는 취소 사유 (필요 없는 경우 null)
     * @param logMessage 로깅할 메시지
     * @param logArgs 로깅 메시지의 인자
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
