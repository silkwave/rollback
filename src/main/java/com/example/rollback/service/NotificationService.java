package com.example.rollback.service;

import com.example.rollback.domain.NotificationLog;
import com.example.rollback.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFailure(Long orderId, String reason) {
        String message = String.format("주문 %d 실패: %s - 고객에게 이메일 발송됨", orderId, reason);
        log.error(message);
        notificationLogRepository.save(new NotificationLog(orderId, message, "FAILURE"));
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendSuccess(Long orderId) {
        String message = String.format("주문 %d 성공 - 고객에게 이메일 발송됨", orderId);
        log.info(message);
        notificationLogRepository.save(new NotificationLog(orderId, message, "SUCCESS"));
    }
}