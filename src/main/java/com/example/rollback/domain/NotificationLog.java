package com.example.rollback.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 알림 로그 엔티티입니다.
 */
@Data
@NoArgsConstructor
@Slf4j
public class NotificationLog {
    /** 알림 로그 고유 ID */
    private Long id;
    
    /** 알림 고유 식별자 (GUID) */
    private String guid;
    
    /** 관련 주문 ID */
    private Long orderId; // 주문 로그용(하위 호환)
    
    /** 알림 메시지 내용 */
    private String message;
    
    /** 알림 타입 */
    private String type;
    
    /** 알림 생성 일시 */
    private LocalDateTime createdAt;

    /**
     * 주문용 로그를 생성합니다. (하위 호환)
     */
    public NotificationLog(String guid, Long orderId, String message, NotificationType type) {
        this.guid = guid;
        this.orderId = orderId;
        this.message = message;
        this.type = type.name();
        this.createdAt = LocalDateTime.now();
        
        log.info("알림 로그 생성 - GUID: {}, 주문ID: {}, 타입: {}, 메시지: {}", guid, orderId, type, message);
    }

    /**
     * 거래용 로그를 생성합니다.
     */
    public NotificationLog(String guid, String message, NotificationType type) {
        this.guid = guid;
        this.orderId = null; // 거래 로그는 주문 ID 미사용
        this.message = message;
        this.type = type.name();
        this.createdAt = LocalDateTime.now();
        
        log.info("알림 로그 생성 - GUID: {}, 타입: {}, 메시지: {}", guid, type, message);
    }
    
    /**
     * 알림 타입입니다.
     */
    public enum NotificationType {
        /**
         * 성공
         */
        SUCCESS, 
        
        /**
         * 실패
         */
        FAILURE, 
        
        /**
         * 정보
         */
        INFO, 
        /**
         * 경고
         */
        WARNING
    }
}
