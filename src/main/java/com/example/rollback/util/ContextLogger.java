package com.example.rollback.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;


/**
 * 컨텍스트를 인식하는 로깅 유틸리티 클래스.
 * 모든 로그 메시지에 자동으로 컨텍스트 정보(GUID 등)를 포함하여 추적성을 높입니다.
 */
@Slf4j
public class ContextLogger {
    
    // 로그 레벨을 나타내는 내부 enum
    private enum LogLevel {
        INFO, DEBUG, WARN, ERROR, TRACE
    }

    /**
     * 중앙 집중식 로깅 헬퍼 메소드.
     * 메시지를 포맷하고 해당 로그 레벨로 디스패치합니다.
     */
    private static void doLog(LogLevel level, String message, Object[] args, Throwable throwable) {
        String formattedMessage = formatMessage(message, args);
        switch (level) {
            case INFO:
                log.info(formattedMessage, throwable);
                break;
            case DEBUG:
                log.debug(formattedMessage, throwable);
                break;
            case WARN:
                log.warn(formattedMessage, throwable);
                break;
            case ERROR:
                log.error(formattedMessage, throwable);
                break;
            case TRACE:
                log.trace(formattedMessage, throwable);
                break;
        }
    }
    
    /**
     * INFO 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void info(String message, Object... args) {
        doLog(LogLevel.INFO, message, args, null);
    }

    /**
     * ERROR 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void error(String message, Object... args) {
        doLog(LogLevel.ERROR, message, args, null);
    }

    /**
     * ERROR 레벨 로그를 기록합니다. 예외 정보와 함께 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param throwable 예외 객체
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void error(String message, Throwable throwable, Object... args) {
        doLog(LogLevel.ERROR, message, args, throwable);
    }

    /**
     * WARN 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void warn(String message, Object... args) {
        doLog(LogLevel.WARN, message, args, null);
    }

    /**
     * DEBUG 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void debug(String message, Object... args) {
        doLog(LogLevel.DEBUG, message, args, null);
    }

    /**
     * TRACE 레벨 로그를 기록합니다. 컨텍스트 정보를 자동으로 포함합니다.
     * 
     * @param message 로그 메시지
     * @param args 메시지 포맷팅을 위한 인자들
     */
    public static void trace(String message, Object... args) {
        doLog(LogLevel.TRACE, message, args, null);
    }

    /**
     * 주문 처리 시작을 위한 전용 로그 메서드.
     * 
     * @param customerName 고객명
     * @param amount 금액
     */
    public static void logOrderStart(String customerName, Integer amount) {
        info("주문 처리를 시작합니다 - 고객: {}, 금액: {}", customerName, amount);
    }

    /**
     * 주문 생성 성공을 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     */
    public static void logOrderCreated(Long orderId) {
        info("주문이 성공적으로 생성되었습니다 - 주문 ID: {}", orderId);
    }

    /**
     * 결제 처리 시작을 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     * @param amount 금액
     */
    public static void logPaymentStart(Long orderId, Integer amount) {
        info("결제 처리를 시작합니다 - 주문 ID: {}, 금액: {}", orderId, amount);
    }

    /**
     * 결제 성공을 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     */
    public static void logPaymentSuccess(Long orderId) {
        info("결제가 성공적으로 완료되었습니다 - 주문 ID: {}", orderId);
    }

    /**
     * 결제 실패를 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     * @param reason 실패 사유
     * @param throwable 예외 객체 (선택사항)
     */
    public static void logPaymentFailure(Long orderId, String reason, Throwable throwable) {
        error("결제에 실패했습니다 - 주문 ID: {}, 사유: {}", throwable, orderId, reason);
    }

    /**
     * 롤백 처리를 위한 전용 로그 메서드.
     * 
     * @param orderId 주문 ID
     * @param reason 롤백 사유
     */
    public static void logRollback(Long orderId, String reason) {
        warn("트랜잭션 롤백이 수행되었습니다 - 주문 ID: {}, 사유: {}", orderId, reason);
    }

    /**
     * 알림 발송을 위한 전용 로그 메서드.
     * 
     * @param notificationType 알림 타입
     * @param message 알림 메시지
     */
    public static void logNotification(String notificationType, String message) {
        info("알림이 발송되었습니다 - 타입: {}, 메시지: {}", notificationType, message);
    }

    /**
     * 비즈니스 로직 단계별 로깅을 위한 전용 메서드.
     * 
     * @param step 처리 단계
     * @param description 단계 설명
     */
    public static void logStep(String step, String description) {
        info("[{}] {}", step, description);
    }

    /**
     * 성능 측정을 위한 전용 로그 메서드.
     * 
     * @param operation 작업명
     * @param durationMs 소요 시간 (밀리초)
     */
    public static void logPerformance(String operation, long durationMs) {
        info("성능: {} - 소요시간: {}ms", operation, durationMs);
    }

    /**
     * 메시지에 컨텍스트 정보를 추가하여 포맷팅합니다.
     * 
     * @param message 원본 메시지
     * @param args 포맷팅 인자들
     * @return 컨텍스트 정보가 포함된 포맷팅된 메시지
     */
    private static String formatMessage(String message, Object... args) {
        // 컨텍스트 정보 추가
        CtxMap context = ContextHolder.getCurrentContext();
        String guid = context.getString("guid");

        // SLF4J 포맷팅을 사용하여 메시지 본문 생성
        String formattedBody = (args != null && args.length > 0) ? MessageFormatter.arrayFormat(message, args).getMessage() : message;
        
        return "[GUID: " + guid + "] " + formattedBody;
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