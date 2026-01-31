package com.example.rollback.controller;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.service.OrderService;
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

    // 주문 생성 엔드포인트
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, BindingResult bindingResult) {
        log.info("\n\n\n\n=======================================================");        
        log.info("POST /api/orders - 요청: {}", request);

        // 유효성 검사
        if (bindingResult.hasErrors()) {
            log.warn("유효성 검사 실패: {}", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage()
            ));
        }

        try {
            // 주문 생성
            Order order = orderService.create(request);
            log.info("주문 생성 성공: {}", order.getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "주문이 성공적으로 생성되었습니다",
                "order", order
            ));
        } catch (Exception e) {
            // 예외 처리
            log.error("주문 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "주문 실패: " + e.getMessage()
            ));
        }
    }

    // 전체 주문 목록 조회
    @GetMapping
    public List<Order> getAllOrders() {
        log.info("\n\n\n\n=======================================================");           
        log.debug("GET /api/orders - 모든 주문 조회 요청");
        return orderRepository.findAll();
    }

    // 특정 주문 조회
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        log.info("\n\n\n\n=======================================================");           
        log.debug("주문 조회 요청: {}", id);
        Order order = orderRepository.findById(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }
}