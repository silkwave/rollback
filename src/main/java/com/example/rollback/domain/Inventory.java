package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// 재고 엔티티
@Slf4j
@Data
public class Inventory {
    private Long id;
    private String productName;
    private Integer currentStock;
    private Integer reservedStock;
    private Integer minStockLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 사용 가능한 재고 계산
    public int getAvailableStock() {
        return currentStock - reservedStock;
    }
    
    // 재고 예약
    public void reserveStock(int quantity) {
        if (getAvailableStock() < quantity) {
            throw new IllegalStateException(
                String.format("재고 부족: %s (요청: %d, 가용: %d)", 
                    productName, quantity, getAvailableStock())
            );
        }
        this.reservedStock += quantity;
        log.info("재고 예약 완료 - 상품: {}, 수량: {}, 예약된 재고: {}", 
            productName, quantity, this.reservedStock);
    }
    
    // 재고 예약 취소
    public void releaseReservation(int quantity) {
        if (this.reservedStock < quantity) {
            throw new IllegalStateException(
                String.format("예약된 재보다 많은 수량을 해제하려 함: %s", productName)
            );
        }
        this.reservedStock -= quantity;
        log.info("재고 예약 해제 - 상품: {}, 수량: {}, 남은 예약 재고: {}", 
            productName, quantity, this.reservedStock);
    }
    
    // 실제 재고 차감
    public void deductStock(int quantity) {
        if (this.reservedStock < quantity) {
            throw new IllegalStateException(
                String.format("예약된 재고 부족: %s", productName)
            );
        }
        this.reservedStock -= quantity;
        this.currentStock -= quantity;
        log.info("재고 차감 완료 - 상품: {}, 수량: {}, 현재 재고: {}, 예약된 재고: {}", 
            productName, quantity, this.currentStock, this.reservedStock);
    }
    
    // 재고 입고
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("입고 수량은 0보다 커야 합니다");
        }
        this.currentStock += quantity;
        log.info("재고 입고 완료 - 상품: {}, 수량: {}, 현재 재고: {}", 
            productName, quantity, this.currentStock);
    }
    
    // 재고 부족 여부 확인
    public boolean isLowStock() {
        return getAvailableStock() <= minStockLevel;
    }
    
    // 재고 데이터 유효성 검사
    public static void validateInventoryData(String productName, Integer currentStock, Integer minStockLevel) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (currentStock == null || currentStock < 0) {
            throw new IllegalArgumentException("현재 재고는 0 이상이어야 합니다");
        }
        if (minStockLevel == null || minStockLevel < 0) {
            throw new IllegalArgumentException("최소 재고 레벨은 0 이상이어야 합니다");
        }
    }
    
    // 재고 생성 팩토리 메서드
    public static Inventory create(String productName, Integer currentStock, Integer minStockLevel) {
        validateInventoryData(productName, currentStock, minStockLevel);
        Inventory inventory = new Inventory();
        inventory.productName = productName;
        inventory.currentStock = currentStock;
        inventory.reservedStock = 0;
        inventory.minStockLevel = minStockLevel;
        log.info("재고 객체 생성됨 - 상품: {}, 재고: {}, 최소 재고: {}", 
            productName, currentStock, minStockLevel);
        return inventory;
    }
}