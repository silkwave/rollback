package com.example.rollback.repository;

import com.example.rollback.domain.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface InventoryRepository {
    
    // 전체 재고 목록 조회
    List<Inventory> findAll();
    
    // 상품명으로 재고 조회
    Optional<Inventory> findByProductName(String productName);
    
    // ID로 재고 조회
    Optional<Inventory> findById(Long id);
    
    // 재고 생성
    int save(Inventory inventory);
    
    // 재고 정보 업데이트
    int update(Inventory inventory);
    
    // 재고 차감 (예약 제외)
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    // 재고 예약
    int reserveStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    // 재고 예약 해제
    int releaseReservation(@Param("id") Long id, @Param("quantity") int quantity);
    
    // 재고 부족 목록 조회
    List<Inventory> findLowStockItems();
    
    // 특정 수량만큼 재고가 있는지 확인
    boolean hasEnoughStock(@Param("productName") String productName, @Param("quantity") int quantity);
}