package com.example.rollback.controller;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.domain.Shipment;
import com.example.rollback.domain.Inventory;
import com.example.rollback.domain.InventoryRequest;
import com.example.rollback.domain.ShipmentRequest;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.service.*;
import com.example.rollback.util.ContextHolder;
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
import org.slf4j.MDC;

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
        String guid = setupRequestContext(httpRequest, "POST /api/orders - 요청: " + request);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            // 주문 생성
            Order order = orderService.create(request);
            log.info("주문 생성 성공: {}", order != null ? order.getId() : "null");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "주문이 성공적으로 생성되었습니다",
                    "order", order));

        } catch (Exception e) {
            log.error("주문 생성 실패: {}", e.getMessage(), e);
            e.printStackTrace(); // 디버깅을 위해 스택트레이스 출력
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "주문 실패: " + e.getMessage()));
        } finally {
            // 컨텍스트 정리
            ContextHolder.clearContext();
            MDC.remove("guid");
            MDC.remove("guid");
        }
    }

    // 전체 주문 목록 조회
    @GetMapping
    public List<Order> getAllOrders(HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/orders - 모든 주문 조회 요청", httpRequest);
        try {
            log.info("모든 주문 목록 조회");
            return orderRepository.findAll();

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 특정 주문 조회
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        initializeContextAndLog("주문 조회 요청: " + id, httpRequest);
        try {
            Order order = orderRepository.findById(id);

            if (order != null) {
                log.info("주문 조회 성공: {}", id);
                return ResponseEntity.ok(order);
            } else {
                log.warn("주문을 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 주문 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id,
            @Valid @RequestBody OrderRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("PUT /api/orders/" + id + " - 주문 수정 요청: " + request, httpRequest);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Order order = orderService.update(id, request);
            log.info("주문 수정 성공: {}", order.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "주문이 성공적으로 수정되었습니다",
                    "order", order));

        } catch (Exception e) {
            log.error("주문 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "주문 수정 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 주문 취소 API
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("POST /api/orders/" + id + "/cancel - 주문 취소 요청", httpRequest);
        try {
            orderService.cancel(id);
            log.info("주문 취소 성공: {}", id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "주문이 성공적으로 취소되었습니다"));

        } catch (Exception e) {
            log.error("주문 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "주문 취소 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 재고 관련 APIs
    @GetMapping("/inventory")
    public List<Inventory> getAllInventory(HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/orders/inventory - 전체 재고 조회 요청", httpRequest);
        try {
            log.info("전체 재고 목록 조회");
            return inventoryService.getAllInventory();

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    @GetMapping("/inventory/low-stock")
    public List<Inventory> getLowStockItems(HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/orders/inventory/low-stock - 재고 부족 목록 조회 요청", httpRequest);
        try {
            log.info("재고 부족 목록 조회");
            return inventoryService.getLowStockItems();

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> createInventory(@Valid @RequestBody InventoryRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("POST /api/orders/inventory - 신규 재고 생성 요청: " + request, httpRequest);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Inventory inventory = inventoryService.createInventory(request.getProductName(), request.getCurrentStock(),
                    request.getMinStockLevel());
            log.info("신규 재고 생성 성공: {}", inventory.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "재고가 성공적으로 생성되었습니다",
                    "inventory", inventory));

        } catch (Exception e) {
            log.error("재고 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "재고 생성 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 배송 관련 APIs
    @GetMapping("/{id}/shipment")
    public ResponseEntity<Shipment> getOrderShipment(@PathVariable Long id, HttpServletRequest httpRequest) {
        initializeContextAndLog("주문별 배송 조회 요청: " + id, httpRequest);
        try {
            return shipmentService.findByOrderId(id)
                    .map(shipment -> {
                        log.info("배송 조회 성공: {}", shipment.getId());
                        return ResponseEntity.ok(shipment);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    @PostMapping("/{id}/shipment")
    public ResponseEntity<?> createShipment(@PathVariable Long id,
            @Valid @RequestBody ShipmentRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("POST /api/orders/" + id + "/shipment - 배송 생성 요청", httpRequest);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Shipment shipment = shipmentService.createShipment(id, request.getShippingAddress());
            log.info("배송 생성 성공: {}", shipment.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "배송이 성공적으로 생성되었습니다",
                    "shipment", shipment));

        } catch (Exception e) {
            log.error("배송 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "배송 생성 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    @PostMapping("/shipment/{shipmentId}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long shipmentId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("POST /api/orders/shipment/" + shipmentId + "/ship - 배송 시작 요청",
                httpRequest);
        try {
            String carrier = request.getOrDefault("carrier", "CJ대한통운");
            Shipment shipment = shipmentService.shipOrder(shipmentId, carrier);
            log.info("배송 시작 성공: {}", shipment.getTrackingNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "배송이 시작되었습니다",
                    "shipment", shipment));

        } catch (Exception e) {
            log.error("배송 시작 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "배송 시작 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    @PostMapping("/shipment/{shipmentId}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long shipmentId, HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("POST /api/orders/shipment/" + shipmentId + "/deliver - 배송 완료 요청",
                httpRequest);
        try {
            Shipment shipment = shipmentService.deliverOrder(shipmentId);
            log.info("배송 완료 성공: {}", shipmentId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "배송이 완료되었습니다",
                    "shipment", shipment));

        } catch (Exception e) {
            log.error("배송 완료 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "배송 완료 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    private String setupRequestContext(HttpServletRequest httpRequest, String operationMessage) {
        String guid = guidQueueUtil.getGUID();
        ContextHolder.initializeContext(guid);
        MDC.put("guid", guid);

        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String sessionId = httpRequest.getSession().getId();

        ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

        log.info("=======================================================");
        log.info("{}", operationMessage);
        return guid;
    }

    private String initializeContextAndLog(String operation, HttpServletRequest httpRequest) {
        String guid = guidQueueUtil.getGUID();
        // String guid = GuidQueueUtil.generateSimpleGuid();
        ContextHolder.initializeContext(guid);
        MDC.put("guid", guid);

        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String sessionId = httpRequest.getSession().getId();

        ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

        log.info("=======================================================");
        log.info("{}", operation);
        return guid;
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