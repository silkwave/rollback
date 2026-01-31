package com.example.rollback.domain;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

// 알림 로그 엔티티
@Data
@NoArgsConstructor
public class NotificationLog {
    private Long id;
    private String guid;
    private Long orderId;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;

    // 알림 로그 생성
    public NotificationLog(String guid, Long orderId, String message, NotificationType type) {
        this.guid = guid;
        this.orderId = orderId;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
    
    // 하위 호환성을 위한 생성자
    public NotificationLog(Long orderId, String message, NotificationType type) {
        this(null, orderId, message, type);
    }
    
    // 알림 타입 열거형
    public enum NotificationType {
        SUCCESS, FAILURE
    }
}