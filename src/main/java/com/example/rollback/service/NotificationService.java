package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/** 사용자에게 알림(이메일 등)을 발송하는 서비스 */
public class NotificationService {
    
    /** 주문 실패 시 알림을 전송합니다. */
    public void sendFailure(Long orderId, String reason) {
        log.info("Sending failure notification for order: {}, Reason: {}", orderId, reason);
        log.info("🧵 Notification Thread: {}", Thread.currentThread().getName());
        log.error("🚨 ORDER FAILED - Order ID: {}, Reason: {}", orderId, reason);
        log.info("📧 Email sent to customer about payment failure");
        log.info("✅ Failure notification process completed for order: {}", orderId);
    }
    
    /** 주문 성공 시 알림을 전송합니다. */
    public void sendSuccess(Long orderId) {
        log.info("Sending success notification for order: {}", orderId);
        log.info("🧵 Notification Thread: {}", Thread.currentThread().getName());
        log.info("✅ ORDER SUCCESS - Order ID: {}", orderId);
        log.info("📧 Email sent to customer about successful order");
        log.info("✅ Success notification process completed for order: {}", orderId);
    }
}