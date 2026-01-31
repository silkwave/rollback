package com.example.rollback.controller;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.domain.Shipment;
import com.example.rollback.domain.Inventory;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.service.*;
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
    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;

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
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
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
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
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
    
    // 주문 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, 
                                       @Valid @RequestBody OrderRequest request,
                                       BindingResult bindingResult,
                                       HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("PUT /api/orders/{} - 주문 수정 요청: {}", id, request);

            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                ContextLogger.warn(errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "guid", guid,
                    "message", errorMessage
                ));
            }

            Order order = orderService.update(id, request);
            ContextLogger.info("주문 수정 성공: {}", order.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "주문이 성공적으로 수정되었습니다",
                "order", order
            ));
            
        } catch (Exception e) {
            ContextLogger.error("주문 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "주문 수정 실패: " + e.getMessage()
            ));
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    // 주문 취소 API
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("POST /api/orders/{}/cancel - 주문 취소 요청", id);

            orderService.cancel(id);
            ContextLogger.info("주문 취소 성공: {}", id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "주문이 성공적으로 취소되었습니다"
            ));
            
        } catch (Exception e) {
            ContextLogger.error("주문 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "주문 취소 실패: " + e.getMessage()
            ));
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    // 재고 관련 APIs
    @GetMapping("/inventory")
    public List<Inventory> getAllInventory(HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("GET /api/orders/inventory - 전체 재고 조회 요청");
            return inventoryService.getAllInventory();
            
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    @GetMapping("/inventory/low-stock")
    public List<Inventory> getLowStockItems(HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("GET /api/orders/inventory/low-stock - 재고 부족 목록 조회 요청");
            return inventoryService.getLowStockItems();
            
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    @PostMapping("/inventory")
    public ResponseEntity<?> createInventory(@RequestBody Map<String, Object> request, 
                                         HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("POST /api/orders/inventory - 신규 재고 생성 요청: {}", request);

            String productName = (String) request.get("productName");
            Integer currentStock = (Integer) request.get("currentStock");
            Integer minStockLevel = request.get("minStockLevel") != null ? 
                (Integer) request.get("minStockLevel") : 10;
            
            Inventory inventory = inventoryService.createInventory(productName, currentStock, minStockLevel);
            ContextLogger.info("신규 재고 생성 성공: {}", inventory.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "재고가 성공적으로 생성되었습니다",
                "inventory", inventory
            ));
            
        } catch (Exception e) {
            ContextLogger.error("재고 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "재고 생성 실패: " + e.getMessage()
            ));
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    // 배송 관련 APIs
    @GetMapping("/{id}/shipment")
    public ResponseEntity<Shipment> getOrderShipment(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("주문별 배송 조회 요청: {}", id);
            return shipmentService.findByOrderId(id)
                .map(shipment -> {
                    ContextLogger.info("배송 조회 성공: {}", shipment.getId());
                    return ResponseEntity.ok(shipment);
                })
                .orElse(ResponseEntity.notFound().build());
            
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    @PostMapping("/{id}/shipment")
    public ResponseEntity<?> createShipment(@PathVariable Long id, 
                                           @RequestBody Map<String, String> request,
                                           HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("POST /api/orders/{}/shipment - 배송 생성 요청", id);

            String shippingAddress = request.get("shippingAddress");
            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "배송지 주소는 필수입니다"
                ));
            }
            
            Shipment shipment = shipmentService.createShipment(id, shippingAddress);
            ContextLogger.info("배송 생성 성공: {}", shipment.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "배송이 성공적으로 생성되었습니다",
                "shipment", shipment
            ));
            
        } catch (Exception e) {
            ContextLogger.error("배송 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "배송 생성 실패: " + e.getMessage()
            ));
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    @PostMapping("/shipment/{shipmentId}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long shipmentId,
                                     @RequestBody Map<String, String> request,
                                     HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("POST /api/orders/shipment/{}/ship - 배송 시작 요청", shipmentId);

            String carrier = request.getOrDefault("carrier", "CJ대한통운");
            Shipment shipment = shipmentService.shipOrder(shipmentId, carrier);
            ContextLogger.info("배송 시작 성공: {}", shipment.getTrackingNumber());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "배송이 시작되었습니다",
                "shipment", shipment
            ));
            
        } catch (Exception e) {
            ContextLogger.error("배송 시작 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "배송 시작 실패: " + e.getMessage()
            ));
        } finally {
            ContextHolder.clearContext();
        }
    }
    
    @PostMapping("/shipment/{shipmentId}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long shipmentId, HttpServletRequest httpRequest) {
        try {
            String guid = GuidQueueUtil.generateSimpleGuid();
            ContextHolder.initializeContext(guid);
            
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            ContextHolder.addClientInfo(clientIp, userAgent, null);
            
            ContextLogger.info("\n\n\n\n=======================================================");
            ContextLogger.info("POST /api/orders/shipment/{}/deliver - 배송 완료 요청", shipmentId);

            Shipment shipment = shipmentService.deliverOrder(shipmentId);
            ContextLogger.info("배송 완료 성공: {}", shipmentId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "배송이 완료되었습니다",
                "shipment", shipment
            ));
            
        } catch (Exception e) {
            ContextLogger.error("배송 완료 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "배송 완료 실패: " + e.getMessage()
            ));
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