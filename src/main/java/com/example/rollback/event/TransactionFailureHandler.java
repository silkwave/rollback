package com.example.rollback.event;

import com.example.rollback.service.NotificationService;
import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.CtxMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * 거래 실패(롤백) 이벤트를 후처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionFailureHandler {

    /** 알림 서비스 */
    private final NotificationService notifier;

    /**
     * 롤백 이후에 비동기로 실행됩니다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async
    public void handle(TransactionFailed event) {
        MDC.put("guid", event.getGuid());
        try {
            // 컨텍스트 복원
            Map<String, Object> context = event.getContext();
            if (context != null && !context.isEmpty()) {
                ContextHolder.setContext(new CtxMap(context));
            } else {
                ContextHolder.initializeContext(event.getGuid());
            }

            log.info("");
            log.info("");
            log.info("");
            log.info("");
            log.info("");
            log.info("==============================================================");
            log.info("[TRANSACTION_ROLLBACK_HANDLER] 롤백 후 거래 실패 이벤트 처리 시작");
            log.info("거래 ID: {}, 실패 사유: {}", event.getTransactionId(), event.getReason());

            // 실패 알림 기록/전송
            notifier.sendTransactionFailure(event.getTransactionId(), event.getReason());

            log.info("[TRANSACTION_ROLLBACK_HANDLER] 롤백 후 거래 실패 이벤트 처리 완료");
            log.info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", "TRANSACTION_FAILURE_NOTIFICATION", "거래 실패 알림 발송 완료");

        } catch (Exception ex) {
            log.error("거래 실패 이벤트 처리 중 예외 발생", ex.getClass().getSimpleName());
        } finally {
            // 컨텍스트 정리
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }
}
