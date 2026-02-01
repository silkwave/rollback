package com.example.rollback.repository;

import com.example.rollback.domain.Inventory;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface InventoryRepository {
    
    // 전체 재고 목록 조회
    @Select("SELECT * FROM inventory ORDER BY product_name")
    List<Inventory> findAll();
    
    // 상품명으로 재고 조회
    @Select("SELECT * FROM inventory WHERE product_name = #{productName}")
    Optional<Inventory> findByProductName(String productName);
    
    // ID로 재고 조회
    @Select("SELECT * FROM inventory WHERE id = #{id}")
    Optional<Inventory> findById(Long id);
    
    // 재고 생성
    @Insert("INSERT INTO inventory (product_name, current_stock, reserved_stock, min_stock_level) " +
            "VALUES (#{productName}, #{currentStock}, #{reservedStock}, #{minStockLevel})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Inventory inventory);
    
    // 재고 정보 업데이트
    @Update("UPDATE inventory SET " +
            "current_stock = #{currentStock}, " +
            "reserved_stock = #{reservedStock}, " +
            "min_stock_level = #{minStockLevel}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int update(Inventory inventory);
    
    // 재고 차감 (예약 제외)
    @Update("UPDATE inventory SET " +
            "current_stock = current_stock - #{quantity}, " +
            "reserved_stock = reserved_stock - #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} " +
            "AND reserved_stock >= #{quantity} " +
            "AND current_stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    // 재고 예약
    @Update("UPDATE inventory SET " +
            "reserved_stock = reserved_stock + #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} " +
            "AND (current_stock - reserved_stock) >= #{quantity}")
    int reserveStock(@Param("id") Long id, @Param("quantity") int quantity);
    
    // 재고 예약 해제
    @Update("UPDATE inventory SET " +
            "reserved_stock = reserved_stock - #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} " +
            "AND reserved_stock >= #{quantity}")
    int releaseReservation(@Param("id") Long id, @Param("quantity") int quantity);
    

    // 재고 부족 목록 조회
    @Select("SELECT * FROM inventory WHERE " +
            "(current_stock - reserved_stock) <= min_stock_level " +
            "ORDER BY (current_stock - reserved_stock)")
    List<Inventory> findLowStockItems();
    
    // 특정 수량만큼 재고가 있는지 확인
    @Select("SELECT CASE WHEN COUNT(*) > 0 AND (current_stock - reserved_stock) >= #{quantity} " +
            "THEN true ELSE false END " +
            "FROM inventory WHERE product_name = #{productName}")
    boolean hasEnoughStock(@Param("productName") String productName, @Param("quantity") int quantity);
    

}