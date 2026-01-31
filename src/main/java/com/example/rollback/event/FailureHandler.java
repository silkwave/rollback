package com.example.rollback.event;

import com.example.rollback.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("\n\n\n\n=======================================================");
        log.info("롤백 후 실패 이벤트 처리 시작 - 주문 ID: {}", event.getOrderId());
        notifier.sendFailure(event.getOrderId(), event.getReason());
        log.info("롤백 후 실패 이벤트 처리 완료 - 주문 ID: {}", event.getOrderId());
    }
}