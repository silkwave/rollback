package com.example.rollback.config;

import com.example.rollback.util.ContextLogger;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * HTTP 요청에 컨텍스트 정보를 추가하는 필터.
 * 모든 요청에 대해 공통 컨텍스트 정보를 설정합니다.
 */
// @Component
// @Order(Ordered.HIGHEST_PRECEDENCE)
public class ContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // 필터 레벨에서 기본 컨텍스트 정보 설정
            String requestId = generateRequestId();
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 컨텍스트에 요청 정보 저장
            com.example.rollback.util.ContextHolder.put("requestId", requestId);
            com.example.rollback.util.ContextHolder.put("clientIp", clientIp);
            if (userAgent != null) {
                com.example.rollback.util.ContextHolder.put("userAgent", userAgent);
            }
            
            ContextLogger.debug("요청 필터 실행 - RequestID: {}, IP: {}", requestId, clientIp);
            
            // 다음 필터로 체인 계속
            chain.doFilter(request, response);
            
        } finally {
            // 요청 처리 완료 후 컨텍스트 정리
            com.example.rollback.util.ContextHolder.clearContext();
        }
    }

    /**
     * 요청별 고유 ID를 생성합니다.
     */
    private String generateRequestId() {
        return String.format("REQ%d%d", 
            System.currentTimeMillis() % 1000000, 
            Thread.currentThread().hashCode() % 1000);
    }

    /**
     * 클라이언트 IP 주소를 추출합니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }
}