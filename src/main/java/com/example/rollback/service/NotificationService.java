package com.example.rollback.service;

import com.example.rollback.domain.NotificationLog;
import com.example.rollback.domain.NotificationLog.NotificationType;
import com.example.rollback.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void sendFailure(String guid, Long orderId, String reason) {
        log.info("[GUID: {}] [NOTIFICATION] 실패 알림 전송 시작", guid);
        
        String message = String.format("주문 %d 실패: %s - 고객에게 이메일 발송됨", orderId, reason);
        log.info("[GUID: {}] 알림이 발송되었습니다 - 타입: {}, 메시지: {}", guid, "FAILURE_EMAIL", message);
        
        notificationLogRepository.save(new NotificationLog(guid, orderId, message, NotificationType.FAILURE));
        log.info("[GUID: {}] [NOTIFICATION] 실패 알림 전송 완료 및 로그 저장", guid);
    }
    
    
}