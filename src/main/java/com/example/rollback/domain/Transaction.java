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
 *   <li>참조번호 자동 생성</li>
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
    
    /** 거래 참조번호 (외부 시스템과의 연동용) */
    private String referenceNumber;
    
    /** 거래 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 거래 완료 일시 */
    private LocalDateTime completedAt;
    
    /** 거래 실패 사유 */
    private String failureReason;
    
    /** 거래 요청 IP 주소 (보안 로깅용) */
    private String ipAddress;
    
    /** 거래 기기 정보 (모바일, 웹 등) */
    private String deviceInfo;
    
    /** 거래 생성자 ID */
    private String createdBy;
    
    /** 거래 승인자 ID (대규모 거래의 경우) */
    private String approvedBy;
    
    /** 거래 승인 일시 */
    private LocalDateTime approvedAt;
    
    /** 거래 채널 (온라인, ATM, 지점 등) */
    private String transactionChannel;
    
    /** 거래 분류 (송금, 결제 등) */
    private String transactionCategory;
    
    /** 거래 수수료 */
    private java.math.BigDecimal feeAmount;
    
    /** 거래 후 잔액 */
    private java.math.BigDecimal balanceAfter;

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

    /**
     * 거래를 성공적으로 완료 처리합니다
     * 
     * <p>거래 상태를 COMPLETED로 변경하고 완료 시간을 기록합니다.</p>
     * 
     * @return 완료된 Transaction 객체
     */
    public Transaction complete() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        log.info("거래 완료 - GUID: {}, 유형: {}, 금액: {}", guid, transactionType, amount);
        return this;
    }

    /**
     * 거래를 실패 처리합니다
     * 
     * @param reason 실패 사유
     * @return 실패 처리된 Transaction 객체
     */
    public Transaction fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
        log.info("거래 실패 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
        return this;
    }

    /**
     * 거래를 취소 처리합니다
     * 
     * @param reason 취소 사유
     * @return 취소된 Transaction 객체
     */
    public Transaction cancel(String reason) {
        this.status = TransactionStatus.CANCELLED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
        log.info("거래 취소 - GUID: {}, 유형: {}, 금액: {}, 사유: {}", guid, transactionType, amount, reason);
        return this;
    }

    /**
     * 거래 참조번호를 생성하거나 반환합니다
     * 
     * <p>참조번호가 없는 경우에만 새로 생성합니다. 형식: TXN + 타임스탬프 + 4자리 랜덤 숫자</p>
     * 
     * @return 거래 참조번호
     */
    public String generateReferenceNumber() {
        if (this.referenceNumber == null) {
            this.referenceNumber = "TXN" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        }
        return this.referenceNumber;
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
}