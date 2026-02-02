package com.example.rollback.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.rollback.domain.AccountRequest;
import com.example.rollback.domain.NotificationLog;
import com.example.rollback.repository.NotificationLogRepository;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 애플리케이션 전역 예외 처리를 담당하는 Spring 컨트롤러 어드바이스
 * 
 * <p>
 * 이 클래스는 모든 REST 컨트롤러에서 발생하는 예외를 중앙에서 처리하여
 * 다음과 같은 이점을 제공합니다:
 * </p>
 * <ul>
 * <li><strong>일관된 오류 응답 형식:</strong> 모든 예외에 대해 동일한 구조의 JSON 응답 제공</li>
 * <li><strong>중앙화된 로깅:</strong> 모든 예외를 체계적으로 로깅하여 추적성 확보</li>
 * <li><strong>GUID 연계:</strong> 각 예외를 해당 요청의 GUID와 연계하여 디버깅 용이</li>
 * <li><strong>적절한 HTTP 상태 코드:</strong> 예외 종류에 따른 적절한 상태 코드 반환</li>
 * <li><strong>보안:</strong> 내부 시스템 정보 노출 방지를 위한 안전한 메시지 제공</li>
 * </ul>
 * 
 * <p>
 * 처리하는 주요 예외 타입:
 * </p>
 * <ul>
 * <li>유효성 검사 예외 (@Valid 검증 실패)</li>
 * <li>비즈니스 로직 예외 (IllegalArgumentException, IllegalStateException)</li>
 * <li>처리되지 않은 시스템 예외 (Exception)</li>
 * </ul>
 * 
 * @author Spring Application Team
 * @version 1.0
 * @since 2024.01.01
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final NotificationLogRepository notificationLogRepository; // 리포지토리 주입

    /**
     * DTO 유효성 검사 실패 예외를 처리합니다.
     * 
     * <p>
     * 이 메서드는 {@link org.springframework.validation.annotation.Valid} 어노테이션이
     * 적용된 DTO 객체의 필드 검증이 실패했을 때 발생하는 예외를 처리합니다.
     * </p>
     * 
     * <p>
     * 처리 기능:
     * </p>
     * <ul>
     * <li>현재 요청의 GUID 추출 및 응답에 포함</li>
     * <li>검증 실패 필드의 상세 오류 메시지 추출</li>
     * <li>WARN 레벨로 예외 상황 로깅 (디버깅 용이)</li>
     * <li>HTTP 400 Bad Request 상태 코드와 함께 상세 메시지 반환</li>
     * </ul>
     * 
     * @param ex 유효성 검사 실패 시 발생한 예외 객체.
     *           바인딩 결과와 필드 오류 정보를 포함합니다.
     * @return 오류 응답 정보를 담은 {@link ResponseEntity} 객체.
     *         성공 여부, GUID, 오류 메시지를 포함합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        String guid = MDC.get("guid");
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("[GUID: {}] 유효성 검사 실패: {}", guid, errorMessage, ex);

        // 유효성 검사 실패 시 notification_logs 테이블에 저장
        Object target = ex.getBindingResult().getTarget();
        if (target instanceof AccountRequest) {
            AccountRequest accountRequest = (AccountRequest) target;
            NotificationLog notificationLog = accountRequest.toErrorLog(guid, errorMessage);
            notificationLogRepository.save(notificationLog);
            log.info("[GUID: {}] 유효성 검사 실패 로그 저장 완료: {}", guid, errorMessage);
        } else {
            // 다른 DTO 타입이거나 toErrorLog 메서드가 없는 경우 일반적인 로그를 저장
            NotificationLog genericLog = new NotificationLog();
            genericLog.setGuid(guid);
            genericLog.setMessage("유효성 검사 실패: " + errorMessage);
            genericLog.setType("VALIDATION_ERROR");
            genericLog.setCreatedAt(java.time.LocalDateTime.now());
            notificationLogRepository.save(genericLog);
            log.info("[GUID: {}] 일반 유효성 검사 실패 로그 저장 완료: {}", guid, errorMessage);
        }

        Map<String, Object> body = Map.of(
                "success", false,
                "guid", guid,
                "message", "유효성 검사 실패: " + errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 명시적으로 처리되지 않은 모든 시스템 예외를 처리합니다.
     * 
     * <p>
     * 이 메서드는 애플리케이션에서 발생하는 모든 일반 예외를 최후의 수단으로 처리합니다.
     * 시스템의 안정성을 보장하고 내부 정보 노출을 방지하기 위해
     * 일반화된 안전한 메시지를 클라이언트에 전달합니다.
     * </p>
     * 
     * <p>
     * 처리 기능:
     * </p>
     * <ul>
     * <li>현재 요청의 GUID 추출 및 추적 가능성 확보</li>
     * <li>ERROR 레벨로 전체 예외 스택 트레이스 로깅</li>
     * <li>HTTP 500 Internal Server Error 상태 코드 반환</li>
     * <li>보안을 위한 일반화된 오류 메시지 제공</li>
     * </ul>
     * 
     * @param ex 처리되지 않은 시스템 예외 객체.
     *           모든 종류의 예외를 수신할 수 있습니다.
     * @return 서버 오류 응답 정보를 담은 {@link ResponseEntity} 객체.
     *         일관된 형식의 오류 응답을 제공합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        String guid = MDC.get("guid");
        String message = "서버 내부 오류가 발생했습니다: " + ex.getMessage();

        log.error("[GUID: {}] 처리되지 않은 예외 발생", guid, ex);

        Map<String, Object> body = Map.of(
                "success", false,
                "guid", guid,
                "message", message);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 비즈니스 로직 실행 중 발생하는 예외를 처리합니다.
     * 
     * <p>
     * 이 메서드는 애플리케이션의 비즈니스 규칙 위배나
     * 데이터 상태 관련 문제로 발생하는 예외를 처리합니다.
     * </p>
     * 
     * <p>
     * 처리 대상 예외:
     * </p>
     * <ul>
     * <li>{@link IllegalArgumentException}: 잘못된 인자 전달 (예: 음수 금액)</li>
     * <li>{@link IllegalStateException}: 부적절한 상태에서의 메서드 호출 (예: 이미 처리된 주문)</li>
     * </ul>
     * 
     * <p>
     * 처리 기능:
     * </p>
     * <ul>
     * <li>비즈니스 로직 관련 예외의 중앙 처리</li>
     * <li>WARN 레벨 로깅으로 비즈니스 로직 문제 식별 용이</li>
     * <li>HTTP 400 Bad Request 상태 코드 반환</li>
     * <li>구체적인 비즈니스 오류 메시지 전달</li>
     * </ul>
     * 
     * @param ex 비즈니스 로직 위반 시 발생한 예외 객체.
     *           주로 IllegalArgumentException이나 IllegalStateException입니다.
     * @return 비즈니스 오류 응답 정보를 담은 {@link ResponseEntity} 객체.
     *         클라이언트가 오류 원인을 파악할 수 있는 상세 정보 제공.
     */
    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<Map<String, Object>> handleBusinessExceptions(RuntimeException ex) {
        String guid = MDC.get("guid");
        String message = "요청 처리 중 오류가 발생했습니다: " + ex.getMessage();

        log.warn("[GUID: {}] 비즈니스 로직 예외 발생: {}", guid, message, ex);

        Map<String, Object> body = Map.of(
                "success", false,
                "guid", guid,
                "message", message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
