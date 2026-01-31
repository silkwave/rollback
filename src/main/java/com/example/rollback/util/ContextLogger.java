package com.example.rollback.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 컨텍스트를 인식하는 로깅 유틸리티 클래스.
 * 모든 로그 메시지에 자동으로 컨텍스트 정보(GUID 등)를 포함하여 추적성을 높입니다.
 */
@Component
@Slf4j
public class ContextLogger {
    
    // private static final Logger log = Logger.getLogger(ContextLogger.class.getName());

            /**
     * INFO 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void info(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.info(formattedMessage);
    }

    /**
     * ERROR 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void error(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.error(formattedMessage);
    }

    /**
     * ERROR 레벨 로그를 기록합니다. 예외 정보와 함께 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param throwable 예외 객체
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void error(String message, Throwable throwable, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.error(formattedMessage, throwable);
    }

    /**
     * WARN 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void warn(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.warn(formattedMessage);
    }

    /**
     * DEBUG 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void debug(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.debug(formattedMessage);
    }

    /**
     * TRACE 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void trace(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        log.trace(formattedMessage);
    }

    /**
     * 주문 처리 시작을 위한 전용 로그 메서드.
     * 
     * @param customerName 고객명
     * @param amount 금액
     */
    public static void logOrderStart(String customerName, Integer amount) {
        String message = "주문 처리를 시작합니다 - 고객: {}, 금액: {}";
        info(message, customerName, amount);
    }

    /**
     * 주문 생성 성공을 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     */
    public static void logOrderCreated(Long orderId) {
        String message = "주문이 성공적으로 생성되었습니다 - 주문 ID: {}";
        info(message, orderId);
    }

    /**
     * 결제 처리 시작을 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     * @param amount 금액
     */
    public static void logPaymentStart(Long orderId, Integer amount) {
        String message = "결제 처리를 시작합니다 - 주문 ID: {}, 금액: {}";
        info(message, orderId, amount);
    }

    /**
     * 결제 성공을 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     */
    public static void logPaymentSuccess(Long orderId) {
        String message = "결제가 성공적으로 완료되었습니다 - 주문 ID: {}";
        info(message, orderId);
    }

    /**
     * 결제 실패를 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     * @param reason 실패 사유
     * @param throwable 예외 객체 (선택사항)
     */
    public static void logPaymentFailure(Long orderId, String reason, Throwable throwable) {
        String message = "결제에 실패했습니다 - 주문 ID: {}, 사유: {}";
        if (throwable != null) {
            error(message, throwable, orderId, reason);
        } else {
            error(message, orderId, reason);
        }
    }

    /**
     * 롤백 처리를 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     * @param reason 롤백 사유
     */
    public static void logRollback(Long orderId, String reason) {
        String message = "트랜잭션 롤백이 수행되었습니다 - 주문 ID: {}, 사유: {}";
        warn(message, orderId, reason);
    }

    /**
     * 알림 발송을 위한 전용 로그 메서드.
     * 
     * @param notificationType 알림 타입
     * @param message 알림 메시지
     */
    public static void logNotification(String notificationType, String message) {
        String logMessage = "알림이 발송되었습니다 - 타입: {}, 메시지: {}";
        info(logMessage, notificationType, message);
    }

    /**
     * 비즈니스 로직 단계별 로깅을 위한 전용 메서드.
     * 
     * @param step 처리 단계
     * @param description 단계 설명
     */
    public static void logStep(String step, String description) {
        String message = "[{}] {}";
        info(message, step, description);
    }

    /**
     * 성능 측정을 위한 전용 로그 메서드.
     * 
     * @param operation 작업명
     * @param durationMs 소요 시간 (밀리초)
     */
    public static void logPerformance(String operation, long durationMs) {
        String message = "성능: {} - 소요시간: {}ms";
        info(message, operation, durationMs);
    }

    /**
     * 메시지에 컨텍스트 정보를 추가하여 포맷팅합니다.
     * 
     * @param message 원본 메시지
     * @param args 포맷팅 인자들
     * @return 컨텍스트 정보가 포함된 포맷팅된 메시지
     */
    private static String formatMessage(String message, Object... args) {
        StringBuilder sb = new StringBuilder();
        
        // 컨텍스트 정보 추가
        CtxMap context = ContextHolder.getCurrentContext();
        String guid = context.getString("guid");
        String requestId = context.getString("requestId");
        
        sb.append("[GUID: ").append(guid).append("]");
        
        if (!requestId.isEmpty()) {
            sb.append("[REQ: ").append(requestId).append("]");
        }
        
        // 클라이언트 정보가 있으면 추가
        String clientIp = context.getString("clientIp");
        if (!clientIp.isEmpty()) {
            sb.append("[IP: ").append(clientIp).append("]");
        }
        
        sb.append(" ").append(message);
        
        // 메시지 포맷팅
        if (args.length > 0) {
            try {
                return String.format(sb.toString(), args);
            } catch (Exception e) {
                // 포맷팅 실패 시 원본 메시지와 인자들을 결합
                sb.append(" [");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(args[i]);
                }
                sb.append("]");
                return sb.toString();
            }
        }
        
        return sb.toString();
    }

    /**
     * 현재 컨텍스트의 전체 내용을 로깅합니다. 디버깅용으로 사용됩니다.
     */
    public static void logCurrentContext() {
        CtxMap context = ContextHolder.getCurrentContext();
        String message = "현재 컨텍스트: {}";
        debug(message, context.asReadOnlyMap());
    }
}