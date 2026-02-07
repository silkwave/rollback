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
 * 알림 전송 및 로그 기록 서비스 (별도 트랜잭션)
 * 
 * <p>
 * 트랜잭션 실패 시 고객에게 알림을 전송하고, 모든 알림 기록을 데이터베이스에 저장합니다.
 * 새로운 트랜잭션(REQUIRES_NEW)에서 실행되어 원본 트랜잭션 롤백에 영향을 받지 않습니다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 * <li>거래 실패 알림 전송 (SMS, 이메일)</li>
 * <li>알림 로그 기록 및 관리</li>
 * <li>하위 호환성 지원 (기존 주문 시스템)</li>
 * </ul>
 * 
 * <p>
 * <strong>트랜잭션 정책:</strong><br>
 * REQUIRES_NEW propagation을 사용하여 호출하는 트랜잭션과 별개의 새로운 트랜잭션에서 실행됩니다.
 * 이를 통해 원본 트랜잭션이 롤백되더라도 알림 로그는 정상적으로 저장됩니다.
 * </p>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** 알림 로그 리포지토리 - 알림 기록 데이터 접근 */
    private final NotificationLogRepository notificationLogRepository;

    /**
     * 실패 알림을 전송하고 로그를 기록합니다 (주문용 - 하위 호환성)
     * 
     * <p>
     * 기존 주문 시스템과의 호환성을 위해 제공되는 메서드입니다.
     * 새로운 트랜잭션에서 실행되어 주문 트랜잭션 롤백에 영향을 받지 않습니다.
     * </p>
     * 
     * @param orderId 실패한 주문의 ID
     * @param reason  실패 사유
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
     * 거래 실패 알림을 전송하고 로그를 기록합니다 (은행용)
     * 
     * <p>
     * 은행 거래 실패 시 고객에게 SMS와 이메일로 알림을 전송하고,
     * 알림 내역을 데이터베이스에 기록합니다.
     * 새로운 트랜잭션에서 실행되어 거래 트랜잭션 롤백에 영향을 받지 않습니다.
     * </p>
     * 
     * @param transactionId 실패한 거래의 ID
     * @param reason        실패 사유
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