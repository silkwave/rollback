package com.example.rollback.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotificationLog {
    private Long id;
    private Long orderId;
    private String message;
    private String type; // SUCCESS or FAILURE
    private LocalDateTime createdAt;

    public NotificationLog(Long orderId, String message, String type) {
        this.orderId = orderId;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}