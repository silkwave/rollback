package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    
    public void sendFailure(Long orderId, String reason) {
        log.info("Sending failure notification for order: {}, Reason: {}", orderId, reason);
        log.error("🚨 ORDER FAILED - Order ID: {}, Reason: {}", orderId, reason);
        log.info("📧 Email sent to customer about payment failure");
    }
    
    public void sendSuccess(Long orderId) {
        log.info("Sending success notification for order: {}", orderId);
        log.info("✅ ORDER SUCCESS - Order ID: {}", orderId);
        log.info("📧 Email sent to customer about successful order");
    }
}