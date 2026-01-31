package com.example.rollback.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * ThreadLocal을 사용하여 요청 범위의 컨텍스트를 관리하는 유틸리티 클래스.
 * 각 스레드는 자신만의 컨텍스트를 가지며, 요청 처리 동안 유지됩니다.
 */
@Slf4j
@Component
public class ContextHolder {

    /** ThreadLocal을 사용하여 각 스레드별 컨텍스트 저장 */
    private static final ThreadLocal<CtxMap> contextHolder = new ThreadLocal<>();

    /**
     * 현재 스레드에 새로운 컨텍스트를 설정합니다.
     * 기본적인 요청 정보를 포함하여 초기화합니다.
     * 
     * @param guid 주문 GUID
     * @return 초기화된 CtxMap 인스턴스
     */
    public static CtxMap initializeContext(String guid) {
        CtxMap context = CtxMap.of(java.util.Map.of(
            "guid", guid,
            "requestId", UUID.randomUUID().toString(),
            "requestTime", Instant.now(),
            "threadName", Thread.currentThread().getName()
        ));
        
        contextHolder.set(context);
        log.debug("[GUID: {}] 새로운 컨텍스트가 초기화되었습니다. Thread: {}", guid, Thread.currentThread().getName());
        return context;
    }

    /**
     * 외부에서 생성된 컨텍스트를 현재 스레드에 설정합니다.
     * 
     * @param context 설정할 컨텍스트
     */
    public static void setContext(CtxMap context) {
        if (context != null) {
        contextHolder.set(context);
        String guid = context.getString("guid", "unknown");
        log.debug("[GUID: {}] 컨텍스트가 설정되었습니다. Thread: {}", guid, Thread.currentThread().getName());
        }
    }

    /**
     * 현재 스레드의 컨텍스트를 반환합니다.
     * 컨텍스트가 없으면 빈 컨텍스트를 생성하여 반환합니다.
     * 
     * @return 현재 컨텍스트
     */
    public static CtxMap getCurrentContext() {
        CtxMap context = contextHolder.get();
        if (context == null) {
            log.debug("컨텍스트가 없어 빈 컨텍스트를 생성합니다. Thread: {}", Thread.currentThread().getName());
            context = CtxMap.empty();
            contextHolder.set(context);
        }
        return context;
    }

    /**
     * 현재 스레드의 컨텍스트가 있는지 확인합니다.
     * 
     * @return 컨텍스트 존재 여부
     */
    public static boolean hasContext() {
        return contextHolder.get() != null;
    }

    /**
     * 현재 컨텍스트에서 GUID를 안전하게 조회합니다.
     * 
     * @return GUID 문자열 또는 "unknown"
     */
    public static String getCurrentGuid() {
        CtxMap context = getCurrentContext();
        return context.getString("guid", "unknown");
    }

    /**
     * 컨텍스트에 키-값 쌍을 추가합니다.
     * 
     * @param key 키
     * @param value 값
     * @return 메소드 체이닝을 위한 현재 컨텍스트
     */
    public static CtxMap put(String key, Object value) {
        CtxMap context = getCurrentContext();
        return context.put(key, value);
    }

    /**
     * 컨텍스트에서 값을 조회합니다.
     * 
     * @param key 키
     * @param type 기대 타입
     * @param <T> 타입 파라미터
     * @return 조회된 값 또는 null
     */
    public static <T> T get(String key, Class<T> type) {
        CtxMap context = getCurrentContext();
        return context.getObject(key, type);
    }

    /**
     * 컨텍스트에서 문자열 값을 조회합니다.
     * 
     * @param key 키
     * @return 문자열 값
     */
    public static String getString(String key) {
        CtxMap context = getCurrentContext();
        return context.getString(key);
    }

    /**
     * 컨텍스트에서 문자열 값을 조회합니다 (기본값 포함).
     * 
     * @param key 키
     * @param defaultValue 기본값
     * @return 문자열 값 또는 기본값
     */
    public static String getString(String key, String defaultValue) {
        CtxMap context = getCurrentContext();
        return context.getString(key, defaultValue);
    }

    /**
     * 컨텍스트에서 정수 값을 조회합니다.
     * 
     * @param key 키
     * @return 정수 값
     */
    public static int getInt(String key) {
        CtxMap context = getCurrentContext();
        return context.getInt(key);
    }

    /**
     * 컨텍스트에서 정수 값을 조회합니다 (기본값 포함).
     * 
     * @param key 키
     * @param defaultValue 기본값
     * @return 정수 값 또는 기본값
     */
    public static int getInt(String key, int defaultValue) {
        CtxMap context = getCurrentContext();
        return context.getInt(key, defaultValue);
    }

    /**
     * 현재 스레드의 컨텍스트를 제거합니다.
     * 요청 처리가 완료된 후 호출해야 합니다.
     */
    public static void clearContext() {
        CtxMap context = contextHolder.get();
        if (context != null) {
            String guid = context.getString("guid", "unknown");
            log.debug("[GUID: {}] 컨텍스트가 제거되었습니다. Thread: {}", guid, Thread.currentThread().getName());
            contextHolder.remove();
        }
    }

    /**
     * 현재 컨텍스트의 복사본을 반환합니다.
     * 비동기 처리 등에서 컨텍스트를 전파할 때 사용합니다.
     * 
     * @return 컨텍스트의 읽기 전용 복사본
     */
    public static CtxMap copyContext() {
        CtxMap current = getCurrentContext();
        return CtxMap.of(current.asReadOnlyMap());
    }

    /**
     * 컨텍스트에 클라이언트 관련 정보를 추가합니다.
     * 
     * @param clientIp 클라이언트 IP
     * @param userAgent User-Agent
     * @param sessionId 세션 ID (선택사항)
     */
    public static void addClientInfo(String clientIp, String userAgent, String sessionId) {
        CtxMap context = getCurrentContext();
        context.put("clientIp", clientIp)
               .put("userAgent", userAgent);
        
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            context.put("sessionId", sessionId);
        }
    }

    /**
     * 컨텍스트에 비즈니스 관련 정보를 추가합니다.
     * 
     * @param customerId 고객 ID
     * @param orderId 주문 ID
     * @param amount 금액
     */
    public static void addBusinessInfo(String customerId, Long orderId, Integer amount) {
        CtxMap context = getCurrentContext();
        context.put("customerId", customerId)
               .put("orderId", orderId)
               .put("amount", amount);
    }

    /**
     * 컨텍스트에 처리 결과 정보를 추가합니다.
     * 
     * @param status 처리 상태
     * @param message 처리 메시지
     */
    public static void addProcessingResult(String status, String message) {
        CtxMap context = getCurrentContext();
        context.put("processingStatus", status)
               .put("processingMessage", message);
    }
}