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

/**
 * 모든 API 요청을 가로채 GUID 및 요청 정보를 설정하고, 요청 완료 후 컨텍스트를 정리하는 필터입니다.
 * 이 필터는 요청 처리의 진입점 역할을 하며, 모든 컨텍스트 관리를 중앙에서 처리합니다.
 */
@Component
@Order(1) // 필터 체인에서 가장 먼저 실행되도록 순서를 지정합니다.
@Slf4j
@RequiredArgsConstructor
public class ContextFilter implements Filter {

    private final GuidQueueUtil guidQueueUtil;

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
     * 클라이언트 IP 주소를 추출합니다. 프록시 및 로드 밸런서를 고려합니다.
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
