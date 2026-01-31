package com.example.rollback.service;

import com.example.rollback.domain.NotificationLog;
import com.example.rollback.domain.NotificationLog.NotificationType;
import com.example.rollback.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 알림 전송 및 로그 기록 서비스 (별도 트랜잭션)
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    
    // 실패 알림 전송 - 새로운 트랜잭션에서 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFailure(Long orderId, String reason) {
        log.info("[NOTIFICATION] 실패 알림 전송 시작");
        
        String message = String.format("주문 %d 실패: %s - 고객에게 이메일 발송됨", orderId, reason);
        log.info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", "FAILURE_EMAIL", message);
        
        notificationLogRepository.save(new NotificationLog(MDC.get("guid"), orderId, message, NotificationType.FAILURE));
        log.info("[NOTIFICATION] 실패 알림 전송 완료 및 로그 저장");
    }
    
    
}