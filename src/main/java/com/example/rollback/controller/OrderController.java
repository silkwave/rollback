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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, BindingResult bindingResult) {
        log.info("POST /api/orders - 요청: {}", request);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage()
            ));
        }

        try {
            Order order = orderService.create(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "주문이 성공적으로 생성되었습니다",
                "order", order
            ));
        } catch (Exception e) {
            log.error("주문 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "주문 실패: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }
}