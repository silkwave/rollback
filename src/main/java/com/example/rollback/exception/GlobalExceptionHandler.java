package com.example.rollback.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 전역 예외 처리기.
 * 모든 컨트롤러에서 발생하는 예외를 중앙에서 처리하여 일관된 오류 응답을 제공합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 어노테이션을 사용한 DTO의 유효성 검사 실패 시 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String guid = MDC.get("guid");
        FieldError error = ex.getBindingResult().getFieldError();
        String errorMessage = "유효성 검사 실패: " + (error != null ? error.getDefaultMessage() : "알 수 없는 오류");
        
        log.warn("[GUID: {}] {}", guid, errorMessage, ex);

        Map<String, Object> body = Map.of(
                "success", false,
                "guid", guid,
                "message", errorMessage
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 처리되지 않은 모든 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        String guid = MDC.get("guid");
        String message = "서버 내부 오류가 발생했습니다: " + ex.getMessage();
        
        log.error("[GUID: {}] 처리되지 않은 예외 발생", guid, ex);

        Map<String, Object> body = Map.of(
                "success", false,
                "guid", guid,
                "message", message
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

     /**
     * 비즈니스 로직 상의 예외를 처리합니다. (예: 계좌 잔액 부족)
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleBusinessExceptions(RuntimeException ex) {
        String guid = MDC.get("guid");
        String message = "요청 처리 중 오류가 발생했습니다: " + ex.getMessage();

        log.warn("[GUID: {}] 비즈니스 로직 예외 발생: {}", guid, message, ex);

        Map<String, Object> body = Map.of(
                "success", false,
                "guid", guid,
                "message", message
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
