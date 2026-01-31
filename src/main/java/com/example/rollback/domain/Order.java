package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// 주문 엔티티
@Slf4j
@Data
public class Order {
    private Long id;
    private String guid;
    private String customerName;
    private Integer amount;
    private String productName;
    private Integer quantity;
    private OrderStatus status = OrderStatus.CREATED;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // public getter for id
    public Long getId() {
        return id;
    }
    
    // 주문 생성 팩토리 메서드
    public static Order create(String guid, String customerName, Integer amount, String productName, Integer quantity) {
        validateOrderData(customerName, amount, productName, quantity);
        Order order = new Order();
        order.guid = guid;
        order.customerName = customerName;
        order.amount = amount;
        order.productName = productName;
        order.quantity = quantity;
        order.status = OrderStatus.CREATED;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        log.info("주문 객체 생성됨 - GUID: {}, 고객: {}, 금액: {}, 상품: {}, 수량: {}", 
            guid, customerName, amount, productName, quantity);
        return order;
    }
    
    // 주문을 결제 완료 상태로 변경
    public Order markAsPaid() {
        this.status = OrderStatus.PAID;
        log.info("주문 {} 상태가 PAID로 변경됨", this.guid);
        return this;
    }
    
    // 결제 완료 상태 확인
    public boolean isPaid() {
        return OrderStatus.PAID.equals(this.status);
    }
    
    // 생성 상태 확인
    public boolean isCreated() {
        return OrderStatus.CREATED.equals(this.status);
    }
    
    // 주문 데이터 유효성 검사
    private static void validateOrderData(String customerName, Integer amount, String productName, Integer quantity) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("고객 이름은 필수입니다");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다");
        }
    }
    
    // 주문 상태 열거형
    public enum OrderStatus {
        CREATED, PAID, PREPARING, SHIPPED, DELIVERED, CANCELLED, FAILED
    }
}