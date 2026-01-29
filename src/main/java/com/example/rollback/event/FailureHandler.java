package com.example.rollback.event;

import com.example.rollback.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailureHandler {

    private final NotificationService notifier;

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_ROLLBACK
    )
    @Async
    public void handle(OrderFailed event) {
        log.info("🔄 ROLLBACK COMPLETED - Executing failure notification");
        notifier.sendFailure(
            event.getOrderId(),
            event.getReason()
        );
    }
}