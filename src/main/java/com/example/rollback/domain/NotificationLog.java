package com.example.rollback.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// 알림 로그 엔티티
@Data
@NoArgsConstructor
@Slf4j
public class NotificationLog {
    private Long id;
    private String guid;
    private Long accountId;
    private Long transactionId;
    private Long orderId;
    private String message;
    private String type;
    private LocalDateTime createdAt;

    // 알림 로그 생성 (주문용 - 하위 호환성)
    public NotificationLog(String guid, Long accountId, Long orderId, String message, NotificationType type) {
        this.guid = guid;
        this.accountId = accountId;
        this.orderId = orderId;
        this.transactionId = null;
        this.message = message;
        this.type = type.name();
        this.createdAt = LocalDateTime.now();
        
        log.info("알림 로그 생성 - GUID: {}, 계좌ID: {}, 주문ID: {}, 타입: {}", guid, accountId, orderId, type);
    }

    // 알림 로그 생성 (거래용)
    public NotificationLog(String guid, Long accountId, Long transactionId, String message, NotificationType type, boolean isTransaction) {
        this.guid = guid;
        this.accountId = accountId;
        this.orderId = null;
        this.transactionId = transactionId;
        this.message = message;
        this.type = type.name();
        this.createdAt = LocalDateTime.now();
        
        log.info("알림 로그 생성 - GUID: {}, 계좌ID: {}, 거래ID: {}, 타입: {}", guid, accountId, transactionId, type);
    }
    
    // 알림 타입 열거형
    public enum NotificationType {
        SUCCESS, FAILURE, INFO, WARNING
    }
}