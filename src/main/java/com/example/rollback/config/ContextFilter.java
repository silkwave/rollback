package com.example.rollback.config;

import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.GuidQueueUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 모든 HTTP 요청을 가로채 컨텍스트 정보를 설정하고 관리하는 서블릿 필터
 * 
 * <p>
 * 이 필터는 Spring 애플리케이션의 모든 HTTP 요청 처리 과정에서
 * 다음과 같은 중요한 역할을 수행합니다:
 * </p>
 * <ul>
 * <li>각 요청에 고유한 GUID(전역 고유 식별자) 할당 및 관리</li>
 * <li>MDC(Mapped Diagnostic Context)에 GUID 설정으로 로그 추적성 확보</li>
 * <li>클라이언트 정보(IP, User-Agent, 세션 ID) 수집 및 저장</li>
 * <li>요청 시작 및 종료 로깅으로 요청 처리 흐름 시각화</li>
 * <li>요청 완료 후 컨텍스트 자동 정리로 메모리 누수 방지</li>
 * </ul>
 * 
 * <p>
 * 이 필터는 필터 체인에서 가장 먼저 실행되어 모든 요청에 대한
 * 일관된 컨텍스트 환경을 보장합니다.
 * </p>
 * 
 * @author Spring Application Team
 * @version 1.0
 * @since 2024.01.01
 */
@Component
@Order(1) // 필터 체인에서 가장 먼저 실행되도록 순서를 지정합니다.
@Slf4j
@RequiredArgsConstructor
public class ContextFilter implements Filter {

    /**
     * GUID 생성 및 관리 유틸리티
     * 
     * <p>
     * 이 의존성은 각 요청에 고유한 GUID를 생성하고
     * GUID의 중복을 방지하기 위해 큐를 관리하는 기능을 제공합니다.
     * </p>
     */
    private final GuidQueueUtil guidQueueUtil;

    /**
     * 모든 HTTP 요청을 가로채 컨텍스트 정보를 설정하고 요청 처리를 관리합니다.
     * 
     * <p>
     * 이 메서드는 다음과 같은 순서로 요청 처리를 수행합니다:
     * </p>
     * <ol>
     * <li><strong>GUID 설정:</strong> 새로운 GUID를 생성하고 MDC와 ContextHolder에 설정</li>
     * <li><strong>클라이언트 정보 수집:</strong> IP 주소, User-Agent, 세션 ID 추출 및 저장</li>
     * <li><strong>요청 로깅:</strong> HTTP 메서드, URI, 클라이언트 정보 상세 기록</li>
     * <li><strong>요청 전달:</strong> 다음 필터 또는 서블릿으로 요청 전달</li>
     * <li><strong>정리:</strong> 요청 완료 후 MDC와 ContextHolder에서 모든 컨텍스트 정보 제거</li>
     * </ol>
     * 
     * <p>
     * try-finally 구조를 사용하여 예외 발생 시에도 항상 컨텍스트가
     * 정리되도록 보장합니다.
     * </p>
     * 
     * @param request  클라이언트로부터 받은 HTTP 요청 객체
     * @param response 클라이언트에게 보낼 HTTP 응답 객체
     * @param chain    다음 필터로 요청을 전달하기 위한 필터 체인
     * @throws IOException      입출력 관련 예외 발생 시
     * @throws ServletException 서블릿 처리 관련 예외 발생 시
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String guid = guidQueueUtil.getGUID();

        try {
            // 1. GUID를 MDC와 ContextHolder 양쪽에 설정합니다.
            MDC.put("guid", guid);
            ContextHolder.initializeContext(guid);

            // 2. 클라이언트 정보를 ContextHolder에 추가합니다.
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String sessionId = httpRequest.getSession().getId();
            ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

            // 3. 요청 정보 로깅 (중앙 집중)
            log.info("==================== REQUEST START =====================");
            log.info("[{}] {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            log.info("Client IP: {}, User-Agent: {}", clientIp, userAgent);

            // 4. 다음 필터 또는 서블릿으로 요청 전달
            chain.doFilter(request, response);

        } finally {
            // 5. 요청 처리가 끝나면 항상 MDC와 ContextHolder를 정리합니다.
            log.info("==================== REQUEST END =======================");
            MDC.clear();
            ContextHolder.clearContext();
        }
    }

    /**
     * HTTP 요청에서 실제 클라이언트 IP 주소를 추출합니다.
     * 
     * <p>
     * 이 메서드는 다양한 네트워크 환경에서 정확한 클라이언트 IP를
     * 식별하기 위해 여러 헤더를 순차적으로 확인합니다:
     * </p>
     * <ul>
     * <li>X-Forwarded-For: 일반적인 프록시/로드밸런서 환경</li>
     * <li>Proxy-Client-IP: WebLogic 프록시 환경</li>
     * <li>WL-Proxy-Client-IP: WebLogic 서버 환경</li>
     * <li>HTTP_CLIENT_IP: 일반적인 프록시 환경</li>
     * <li>HTTP_X_FORWARDED_FOR: 비표준 프록시 환경</li>
     * <li>getRemoteAddr(): 직접 연결 환경 (최후의 수단)</li>
     * </ul>
     * 
     * <p>
     * 이 방식을 통해 CDN, 로드밸런서, 프록시 서버 등을 거치는
     * 복잡한 네트워크 환경에서도 실제 클라이언트 IP를 정확히 파악할 수 있습니다.
     * </p>
     * 
     * @param request 클라이언트 IP를 추출할 HTTP 요청 객체
     * @return 식별된 클라이언트 IP 주소 (확인 불가 시 "unknown" 반환)
     */
    private String getClientIp(HttpServletRequest request) {

        log.info("========== [HTTP HEADER TRACE START] ==========");

        Collections.list(request.getHeaderNames()).stream()
                   .forEach(headerName -> Collections.list(request.getHeaders(headerName)).stream()
                                                     .forEach(value -> log.info("HEADER >> {} = {}", headerName, value)));

        log.info("========== [HTTP HEADER TRACE END] ==========");

        // ⭐ 실제 Client IP 추출 (우선순위 순)
        String ip = extractIp(request);

        log.info("CLIENT IP >> {}", ip);

        return ip;
    }

    private static String extractIp(HttpServletRequest request) {
        String[] headerCandidates = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerCandidates) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {

                // X-Forwarded-For: client, proxy1, proxy2
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
