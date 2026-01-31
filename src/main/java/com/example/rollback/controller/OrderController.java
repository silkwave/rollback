package com.example.rollback.controller;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.service.OrderService;
import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.ContextLogger;
import com.example.rollback.util.GuidQueueUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 주문 관련 REST API 컨트롤러
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final GuidQueueUtil guidQueueUtil;

    // 주문 생성 엔드포인트
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, BindingResult bindingResult, 
                                        HttpServletRequest httpRequest) {
        try {
            // 컨텍스트 초기화
            String guid = guidQueueUtil.getGUID();
            ContextHolder.initializeContext(guid);
            
            // 클라이언트 정보 추가
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String sessionId = httpRequest.getSession().getId();
            ContextHolder.addClientInfo(clientIp, userAgent, sessionId);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("POST /api/orders - 요청: {}", request);

            // 유효성 검사
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                ContextLogger.warn(errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "guid", guid,
                    "message", errorMessage
                ));
            }

            // 주문 생성
            Order order = orderService.create(request);
            ContextLogger.info("주문 생성 성공: {}", order != null ? order.getId() : "null");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "주문이 성공적으로 생성되었습니다",
                "order", order
            ));
            
        } catch (Exception e) {
            ContextLogger.error("주문 생성 실패: {}", e.getMessage(), e);
            e.printStackTrace(); // 디버깅을 위해 스택트레이스 출력
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "주문 실패: " + e.getMessage()
            ));
        } finally {
            // 컨텍스트 정리
            ContextHolder.clearContext();
        }
    }

    // 전체 주문 목록 조회
    @GetMapping
    public List<Order> getAllOrders(HttpServletRequest httpRequest) {
        try {
            // 간단한 컨텍스트 초기화 (조회용)
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            ContextHolder.addClientInfo(clientIp, null, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("GET /api/orders - 모든 주문 조회 요청");
            return orderRepository.findAll();
            
        } finally {
            ContextHolder.clearContext();
        }
    }

    // 특정 주문 조회
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // 간단한 컨텍스트 초기화 (조회용)
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            ContextHolder.addClientInfo(clientIp, null, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("주문 조회 요청: {}", id);
            Order order = orderRepository.findById(id);
            
            if (order != null) {
                ContextLogger.info("주문 조회 성공: {}", id);
                return ResponseEntity.ok(order);
            } else {
                ContextLogger.warn("주문을 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }
            
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    /**
     * 클라이언트 IP 주소를 추출하는 헬퍼 메서드.
     * 
     * @param request HTTP 요청 객체
     * @return 클라이언트 IP 주소
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