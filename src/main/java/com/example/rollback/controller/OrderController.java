package com.example.rollback.controller;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
/** 주문 관련 HTTP 요청을 처리하는 컨트롤러 */
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    /** 새로운 주문을 생성합니다. */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("➡️ POST /api/orders - request: {}", request);
        try {
            // 주문 생성 비즈니스 로직 호출
            Order order = orderService.create(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order created successfully",
                "order", order
            ));
        } catch (Exception e) {
            log.error("🚨 Order creation failed: {}", e.getMessage());
            // 주문 생성 중 예외 발생 시 실패 응답 반환
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Order failed: " + e.getMessage()
            ));
        }
    }

    /** 모든 주문 목록을 조회합니다. */
    @GetMapping
    public List<Order> getAllOrders() {
        log.info("➡️ GET /api/orders");
        return orderRepository.findAll();
    }

    /** ID로 특정 주문을 조회합니다. */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        log.info("➡️ GET /api/orders/{}", id);
        Order order = orderRepository.findById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }
}