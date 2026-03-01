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

/**
 * 요청 단위 컨텍스트(GUID 등)를 세팅/정리하는 필터입니다.
 */
@Component
@Order(1) // 최우선 실행
@Slf4j
@RequiredArgsConstructor
public class ContextFilter implements Filter {

    /**
     * GUID 발급기입니다.
     */
    private final GuidQueueUtil guidQueueUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String guid = guidQueueUtil.getGUID();

        try {
            // MDC/컨텍스트 세팅
            MDC.put("guid", guid);
            ContextHolder.initializeContext(guid);

            // 클라이언트 정보 수집
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String sessionId = httpRequest.getSession().getId();
            ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

            // 요청 시작 로그
            log.info("==================== REQUEST START =====================");
            log.info("[{}] {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            log.info("Client IP: {}, User-Agent: {}", clientIp, userAgent);

            // 다음 필터로 전달
            chain.doFilter(request, response);

        } finally {
            // 요청 종료 및 정리
            log.info("==================== REQUEST END =======================");
            MDC.clear();
            ContextHolder.clearContext();
        }
    }

    private String getClientIp(HttpServletRequest request) {

        log.info("========== [HTTP HEADER TRACE START] ==========");

        Collections.list(request.getHeaderNames()).stream()
                   .forEach(headerName -> Collections.list(request.getHeaders(headerName)).stream()
                                                     .forEach(value -> log.info("HEADER >> {} = {}", headerName, value)));

        log.info("========== [HTTP HEADER TRACE END] ==========");

        // 클라이언트 IP 추출
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

                // X-Forwarded-For는 첫 IP 사용
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
