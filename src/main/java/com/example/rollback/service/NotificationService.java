package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    
    public void sendFailure(Long orderId, String reason) {
        log.error("Order {} failed: {} - Email sent to customer", orderId, reason);
    }
    
    public void sendSuccess(Long orderId) {
        log.info("Order {} succeeded - Email sent to customer", orderId);
    }
}