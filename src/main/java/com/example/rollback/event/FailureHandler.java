package com.example.rollback.event;

import com.example.rollback.service.NotificationService;
import com.example.rollback.util.ContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 주문 실패 이벤트 리스너 - 롤백 후에만 실행
@Slf4j
@Component
@RequiredArgsConstructor
public class FailureHandler {

    private final NotificationService notifier;

    // 롤백 완료 후 비동기적으로 이벤트 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async
    public void handle(OrderFailed event) {
        MDC.put("guid", event.getGuid());
        try {
            // 이벤트에 포함된 컨텍스트 설정
            if (event.getContext() != null && !event.getContext().isEmpty()) {
                ContextHolder.setContext(event.getContext());
            } else {
                // 하위 호환성: 컨텍스트가 없는 경우 GUID로 초기화
                ContextHolder.initializeContext(event.getGuid());
            }
            
            
            log.info("=======================================================");
            log.info("[ROLLBACK_HANDLER] 롤백 후 실패 이벤트 처리 시작");
            log.info("주문 ID: {}, 실패 사유: {}", event.getOrderId(), event.getReason());
            
            // 알림 발송
            notifier.sendFailure(event.getOrderId(), event.getReason());
            
            
            log.info("[ROLLBACK_HANDLER] 롤백 후 실패 이벤트 처리 완료");
            log.info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", "FAILURE_NOTIFICATION", "결제 실패 알림 발송 완료");
            
        } catch (Exception e) {
            log.error("실패 이벤트 처리 중 예외 발생: {}", e.getMessage(), e);
        } finally {
            // 컨텍스트 정리
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }
}