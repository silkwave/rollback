package com.example.rollback.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.rollback.domain.NotificationLog;
import com.example.rollback.repository.NotificationLogRepository;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외를 표준 응답으로 변환합니다.
 * 응답에 GUID를 포함합니다.
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 유효성 실패 등을 알림 로그로 저장합니다. */
    private final NotificationLogRepository notificationLogRepository;

    private static Map<String, Object> errorBody(String guid, String message) {
        return Map.of(
                "success", false,
                "guid", guid,
                "message", message);
    }

    /**
     * 유효성 검사(@Valid) 실패를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        String guid = MDC.get("guid");
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("[GUID: {}] 유효성 검사 실패: {}", guid, errorMessage, ex);

        // 유효성 실패 로그 저장
        NotificationLog genericLog = new NotificationLog();
        genericLog.setGuid(guid);
        genericLog.setMessage("유효성 검사 실패: " + errorMessage);
        genericLog.setType("VALIDATION_ERROR");
        genericLog.setCreatedAt(java.time.LocalDateTime.now());
        notificationLogRepository.save(genericLog);
        log.info("[GUID: {}] 일반 유효성 검사 실패 로그 저장 완료: {}", guid, errorMessage);

        Map<String, Object> body = errorBody(guid, "유효성 검사 실패: " + errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 처리되지 않은 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        String guid = MDC.get("guid");
        String message = "서버 내부 오류가 발생했습니다: " + ex.getMessage();

        log.error("[GUID: {}] 처리되지 않은 예외 발생: {}", guid, ex.getClass().getSimpleName(), ex);

        Map<String, Object> body = errorBody(guid, message);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 입력/상태 오류를 처리합니다.
     */
    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<Map<String, Object>> handleBusinessExceptions(RuntimeException ex) {
        String guid = MDC.get("guid");
        String message = "요청 처리 중 오류가 발생했습니다: " + ex.getMessage();

        log.warn("[GUID: {}] 비즈니스 로직 예외 발생: {}", guid, message, ex);

        Map<String, Object> body = errorBody(guid, message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
