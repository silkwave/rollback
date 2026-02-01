package com.example.rollback.repository;

import com.example.rollback.domain.Shipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface ShipmentRepository {
    
    // 주문 ID로 배송 조회
    Optional<Shipment> findByOrderId(Long orderId);
    
    // ID로 배송 조회
    Optional<Shipment> findById(Long id);
    
    // 배송 생성
    int save(Shipment shipment);
    
    // 배송 정보 업데이트
    int update(Shipment shipment);
    
    // 배송 상태 업데이트
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    // 배송 시작 (운송장번호, 운송사, 배송 시작 시간 업데이트)
    int markAsShipped(@Param("id") Long id, 
                     @Param("trackingNumber") String trackingNumber, 
                     @Param("carrier") String carrier, 
                     @Param("estimatedDelivery") LocalDate estimatedDelivery);
    
    // 배송 완료
    int markAsDelivered(@Param("id") Long id);
    
    // 배송 취소
    int markAsCancelled(@Param("id") Long id);
}