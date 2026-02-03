package com.example.rollback.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 알림 로그 엔티티 클래스
 * 
 * <p>시스템에서 발생하는 모든 알림의 기록을 관리합니다. 거래 알림, 주문 알림,
 * 시스템 알림 등 다양한 유형의 알림을 기록하고 추적할 수 있습니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>거래 관련 알림 기록</li>
 *   <li>주문 관련 알림 기록</li>
 *   <li>알림 타입별 분류</li>
 *   <li>알림 발생 시간 추적</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024-01-01
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
    private Long orderId; // Keep this one
    
    /** 알림 메시지 내용 */
    private String message;
    
    /** 알림 타입 */
    private String type;
    
    /** 알림 생성 일시 */
    private LocalDateTime createdAt;

    /**
     * 주문용 알림 로그를 생성합니다 (하위 호환성 유지)
     * 
     * @param guid 알림 고유 식별자
     * @param orderId 관련 주문 ID
     * @param message 알림 메시지 (관련 ID 정보 포함)
     * @param type 알림 타입
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
     * 거래용 알림 로그를 생성합니다
     * 
     * @param guid 알림 고유 식별자
     * @param message 알림 메시지 (관련 ID 정보 포함)
     * @param type 알림 타입
     */
    public NotificationLog(String guid, String message, NotificationType type) {
        this.guid = guid;
        this.orderId = null; // No orderId for transaction-related logs by default
        this.message = message;
        this.type = type.name();
        this.createdAt = LocalDateTime.now();
        
        log.info("알림 로그 생성 - GUID: {}, 타입: {}, 메시지: {}", guid, type, message);
    }
    
    /**
     * 알림 타입 열거형
     */
    public enum NotificationType {
        /**
         * 성공 알림
         * <p>거래 성공, 작업 완료 등 긍정적인 결과에 대한 알림입니다.</p>
         */
        SUCCESS, 
        
        /**
         * 실패 알림
         * <p>거래 실패, 에러 발생 등 부정적인 결과에 대한 알림입니다.</p>
         */
        FAILURE, 
        
        /**
         * 정보 알림
         * <p>일반적인 정보 전달 목적의 알림입니다.</p>
         */
        INFO, 
        /**
         * 경고 알림
         * <p>주의가 필요한 상황에 대한 알림입니다.</p>
         */
        WARNING
    }
}
