package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    
    public void sendFailure(Long orderId, String reason) {
        log.error("주문 {} 실패: {} - 고객에게 이메일 발송됨", orderId, reason);
    }
    
    public void sendSuccess(Long orderId) {
        log.info("주문 {} 성공 - 고객에게 이메일 발송됨", orderId);
    }
}