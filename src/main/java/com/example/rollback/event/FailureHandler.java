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
/**
 * 주문 실패 이벤트를 처리하는 핸들러.
 * 트랜잭션이 롤백된 후에 비동기적으로 실패 알림을 보냅니다.
 */
public class FailureHandler {

    private final NotificationService notifier;

    /**
     * OrderFailed 이벤트를 수신하여 처리합니다.
     *
     * @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
     *   - 이 리스너는 관련 트랜잭션이 성공적으로 롤백된 후에만 실행됩니다.
     *   - 이를 통해, 데이터베이스 상태가 원상 복구된 것을 보장한 뒤 알림 등의 후속 조치를 안전하게 수행할 수 있습니다.
     *
     * @Async
     *   - 알림 발송과 같은 외부 시스템 연동이나 시간이 소요될 수 있는 작업을 별도의 스레드에서 비동기적으로 처리합니다.
     *   - 이를 통해 원래의 요청 처리 스레드가 빠르게 응답할 수 있도록 합니다.
     */
    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_ROLLBACK
    )
    @Async
    public void handle(OrderFailed event) {
        log.info("🔄 ROLLBACK COMPLETED - Executing failure notification");
        // 이벤트로부터 주문 ID와 실패 원인을 받아 알림 서비스를 호출합니다.
        notifier.sendFailure(
            event.getOrderId(),
            event.getReason()
        );
    }
}