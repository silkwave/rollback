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
 * 거래 실패 이벤트 리스너 - 롤백 후에만 실행
 * 
 * <p>TransactionFailed 이벤트를 비동기적으로 처리하는 Spring 컴포넌트입니다.
 * {@code TransactionPhase.AFTER_ROLLBACK} 설정으로 인해 트랜잭션이 롤백된 후에만 실행되며,
 * {@code @Async}로 비동기 처리되어 원본 작업에 영향을 주지 않습니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>트랜잭션 롤백 완료 후 이벤트 처리</li>
 *   <li>컨텍스트 정보 복원 및 전파</li>
 *   <li>비동기 알림 발송 (SMS, 이메일)</li>
 *   <li>MDC 기반 로깅 연동</li>
 * </ul>
 * 
 * <p><strong>실행 순서:</strong><br>
 * 1. 트랜잭션 실패 → 롤백<br>
 * 2. TransactionFailed 이벤트 발행<br>
 * 3. 이 리스너가 비동기적으로 이벤트 처리<br>
 * 4. 컨텍스트 복원 후 알림 발송</p>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionFailureHandler {

    /** 알림 서비스 - 실패 알림 전송용 */
    private final NotificationService notifier;

    /**
     * 롤백 완료 후 비동기적으로 이벤트를 처리합니다.
     * 
     * <p>트랜잭션이 롤백된 후에만 이 메서드가 호출됩니다.
     * 원본 컨텍스트 정보를 복원하여 실패 알림을 전송하고,
     * 모든 처리가 완료되면 컨텍스트를 정리합니다.</p>
     * 
     * @param event 거래 실패 이벤트 정보
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async 
    public void handle(TransactionFailed event) {
        MDC.put("guid", event.getGuid());
        try {
            // 이벤트에 포함된 컨텍스트 설정
            Map<String, Object> context = event.getContext();
            if (context != null && !context.isEmpty()) {
                // Map을 CtxMap으로 캐스팅하여 타입 안전성 보장
                ContextHolder.setContext(new CtxMap(context));
            } else {
                // 하위 호환성: 컨텍스트가 없는 경우 GUID로 초기화
                ContextHolder.initializeContext(event.getGuid());
            }
            
            log.info("=======================================================");
            log.info("[TRANSACTION_ROLLBACK_HANDLER] 롤백 후 거래 실패 이벤트 처리 시작");
            log.info("거래 ID: {}, 실패 사유: {}", event.getTransactionId(), event.getReason());
            
            // 실패 알림 발송 (SMS + 이메일)
            notifier.sendTransactionFailure(event.getTransactionId(), event.getReason());
            
            log.info("[TRANSACTION_ROLLBACK_HANDLER] 롤백 후 거래 실패 이벤트 처리 완료");
            log.info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", "TRANSACTION_FAILURE_NOTIFICATION", "거래 실패 알림 발송 완료");
            
        } catch (Exception ex) {
            log.error("거래 실패 이벤트 처리 중 예외 발생", ex.getClass().getSimpleName());
        } finally {
            // 컨텍스트 정리 - 메모리 누수 방지
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }
}