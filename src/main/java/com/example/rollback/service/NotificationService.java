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

/**
 * 알림 전송 및 로그 저장을 담당합니다.
 * 알림 로그는 {@code REQUIRES_NEW}로 저장해 원본 트랜잭션과 분리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** 알림 로그 저장소 */
    private final NotificationLogRepository notificationLogRepository;

    /**
     * 주문 실패 알림을 기록합니다. (하위 호환)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFailure(Long orderId, String reason) {

        log.info("");
        log.info("");
        log.info("");
        log.info("");
        log.info("");
        log.info("==============================================================");

        log.info("[NOTIFICATION] 실패 알림 전송 시작");

        String message = String.format("주문 %d 실패: %s - 고객에게 이메일 발송됨", orderId, reason);
        log.info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", "FAILURE_EMAIL", message);

        notificationLogRepository
                .save(new NotificationLog(MDC.get("guid"), orderId, message, NotificationType.FAILURE));
        log.info("[NOTIFICATION] 실패 알림 전송 완료 및 로그 저장");
    }

    /**
     * 거래 실패 알림을 기록합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendTransactionFailure(Long transactionId, String reason) {

        log.info("");
        log.info("");
        log.info("");
        log.info("");
        log.info("");
        log.info("==============================================================");        
        log.info("[NOTIFICATION] 거래 실패 알림 전송 시작");

        String message = String.format("거래 %d 실패: %s - 고객에게 SMS 및 이메일 발송됨", transactionId, reason);
        log.info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", "TRANSACTION_FAILURE_EMAIL", message);

        notificationLogRepository.save(new NotificationLog(MDC.get("guid"), message, NotificationType.FAILURE));
        log.info("[NOTIFICATION] 거래 실패 알림 전송 완료 및 로그 저장");
    }
}
