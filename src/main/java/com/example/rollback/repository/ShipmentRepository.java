package com.example.rollback.repository;

import com.example.rollback.domain.Shipment;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface ShipmentRepository {
    

    // 주문 ID로 배송 조회
    @Select("SELECT * FROM shipments WHERE order_id = #{orderId}")
    Optional<Shipment> findByOrderId(Long orderId);
    
    // ID로 배송 조회
    @Select("SELECT * FROM shipments WHERE id = #{id}")
    Optional<Shipment> findById(Long id);
    

    // 배송 생성
    @Insert("INSERT INTO shipments (order_id, tracking_number, carrier, status, shipping_address, estimated_delivery) " +
            "VALUES (#{orderId}, #{trackingNumber}, #{carrier}, #{status}, #{shippingAddress}, #{estimatedDelivery})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Shipment shipment);
    
    // 배송 정보 업데이트
    @Update("UPDATE shipments SET " +
            "tracking_number = #{trackingNumber}, " +
            "carrier = #{carrier}, " +
            "status = #{status}, " +
            "shipping_address = #{shippingAddress}, " +
            "estimated_delivery = #{estimatedDelivery}, " +
            "shipped_at = #{shippedAt}, " +
            "delivered_at = #{deliveredAt}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int update(Shipment shipment);
    
    // 배송 상태 업데이트
    @Update("UPDATE shipments SET " +
            "status = #{status}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    // 배송 시작 (운송장번호, 운송사, 배송 시작 시간 업데이트)
    @Update("UPDATE shipments SET " +
            "tracking_number = #{trackingNumber}, " +
            "carrier = #{carrier}, " +
            "status = 'SHIPPED', " +
            "shipped_at = CURRENT_TIMESTAMP, " +
            "estimated_delivery = #{estimatedDelivery}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int markAsShipped(@Param("id") Long id, @Param("trackingNumber") String trackingNumber, 
                     @Param("carrier") String carrier, @Param("estimatedDelivery") java.time.LocalDate estimatedDelivery);
    
    // 배송 완료
    @Update("UPDATE shipments SET " +
            "status = 'DELIVERED', " +
            "delivered_at = CURRENT_TIMESTAMP, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int markAsDelivered(@Param("id") Long id);
    
    // 배송 취소
    @Update("UPDATE shipments SET " +
            "status = 'CANCELLED', " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int markAsCancelled(@Param("id") Long id);
    




}