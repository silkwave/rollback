package com.example.rollback.service;

import com.example.rollback.domain.Shipment;
import com.example.rollback.repository.ShipmentRepository;
import com.example.rollback.util.ContextLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {
    
    private final ShipmentRepository shipmentRepository;
    
    // 전체 배송 목록 조회
    public List<Shipment> getAllShipments() {
        ContextLogger.info("전체 배송 목록 조회 요청");
        return shipmentRepository.findAll();
    }
    
    // 주문 ID로 배송 조회
    public Optional<Shipment> findByOrderId(Long orderId) {
        ContextLogger.info("주문별 배송 조회 요청 - 주문 ID: {}", orderId);
        return shipmentRepository.findByOrderId(orderId);
    }
    
    // 운송장번호로 배송 조회
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        ContextLogger.info("운송장번호로 배송 조회 요청 - 운송장번호: {}", trackingNumber);
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }
    
    // 새로운 배송 생성 (트랜잭션)
    @Transactional
    public Shipment createShipment(Long orderId, String shippingAddress) {
        ContextLogger.info("신규 배송 생성 시작 - 주문 ID: {}, 배송지: {}", orderId, shippingAddress);
        
        // 이미 해당 주문에 배송이 있는지 확인
        Optional<Shipment> existingShipment = shipmentRepository.findByOrderId(orderId);
        if (existingShipment.isPresent()) {
            throw new IllegalStateException("이미 배송이 생성된 주문입니다: " + orderId);
        }
        
        Shipment shipment = Shipment.create(orderId, shippingAddress);
        int affectedRows = shipmentRepository.save(shipment);
        
        if (affectedRows == 0) {
            throw new IllegalStateException("배송 생성에 실패했습니다: 주문 ID " + orderId);
        }
        
        // 생성된 배송 정보 다시 조회
        Shipment createdShipment = shipmentRepository.findById(shipment.getId())
            .orElseThrow(() -> new IllegalStateException("생성된 배송 정보를 조회할 수 없습니다"));
        
        ContextLogger.info("신규 배송 생성 완료 - 주문 ID: {}, 배송 ID: {}", orderId, createdShipment.getId());
        
        return createdShipment;
    }
    
    // 배송 시작 (트랜잭션)
    @Transactional
    public Shipment shipOrder(Long shipmentId, String carrier) {
        ContextLogger.info("배송 시작 - 배송 ID: {}, 운송사: {}", shipmentId, carrier);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            throw new IllegalArgumentException("배송을 찾을 수 없습니다: " + shipmentId);
        }
        
        Shipment shipment = shipmentOpt.get();
        
        if (!shipment.isPrepared()) {
            throw new IllegalStateException("배송 준비 상태가 아닙니다: " + shipment.getStatus());
        }
        
        // 운송장번호 생성
        String trackingNumber = Shipment.generateTrackingNumber(carrier);
        LocalDate estimatedDelivery = LocalDate.now().plusDays(3);
        
        // 데이터베이스에서 배송 시작 처리
        int affectedRows = shipmentRepository.markAsShipped(shipmentId, trackingNumber, carrier, estimatedDelivery);
        if (affectedRows == 0) {
            throw new IllegalStateException("배송 시작에 실패했습니다: " + shipmentId);
        }
        
        // 업데이트된 배송 정보 다시 조회
        Shipment updatedShipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new IllegalStateException("업데이트된 배송 정보를 조회할 수 없습니다"));
        
        ContextLogger.info("배송 시작 완료 - 배송 ID: {}, 운송장번호: {}, 운송사: {}", 
            shipmentId, trackingNumber, carrier);
        
        return updatedShipment;
    }
    
    // 배송 완료 (트랜잭션)
    @Transactional
    public Shipment deliverOrder(Long shipmentId) {
        ContextLogger.info("배송 완료 처리 - 배송 ID: {}", shipmentId);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            throw new IllegalArgumentException("배송을 찾을 수 없습니다: " + shipmentId);
        }
        
        Shipment shipment = shipmentOpt.get();
        
        if (shipment.isDelivered()) {
            throw new IllegalStateException("이미 배송 완료된 주문입니다: " + shipmentId);
        }
        
        if (shipment.isCancelled()) {
            throw new IllegalStateException("취소된 배송입니다: " + shipmentId);
        }
        
        // 데이터베이스에서 배송 완료 처리
        int affectedRows = shipmentRepository.markAsDelivered(shipmentId);
        if (affectedRows == 0) {
            throw new IllegalStateException("배송 완료 처리에 실패했습니다: " + shipmentId);
        }
        
        // 업데이트된 배송 정보 다시 조회
        Shipment updatedShipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new IllegalStateException("업데이트된 배송 정보를 조회할 수 없습니다"));
        
        ContextLogger.info("배송 완료 처리 완료 - 배송 ID: {}", shipmentId);
        
        return updatedShipment;
    }
    
    // 배송 취소 (트랜잭션)
    @Transactional
    public Shipment cancelShipment(Long shipmentId) {
        ContextLogger.info("배송 취소 처리 - 배송 ID: {}", shipmentId);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            throw new IllegalArgumentException("배송을 찾을 수 없습니다: " + shipmentId);
        }
        
        Shipment shipment = shipmentOpt.get();
        
        if (shipment.isDelivered()) {
            throw new IllegalStateException("이미 배송 완료된 주문은 취소할 수 없습니다: " + shipmentId);
        }
        
        // 데이터베이스에서 배송 취소 처리
        int affectedRows = shipmentRepository.markAsCancelled(shipmentId);
        if (affectedRows == 0) {
            throw new IllegalStateException("배송 취소 처리에 실패했습니다: " + shipmentId);
        }
        
        // 업데이트된 배송 정보 다시 조회
        Shipment updatedShipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new IllegalStateException("업데이트된 배송 정보를 조회할 수 없습니다"));
        
        ContextLogger.info("배송 취소 처리 완료 - 배송 ID: {}", shipmentId);
        
        return updatedShipment;
    }
    
    // 배송 중인 목록 조회
    public List<Shipment> getInTransitShipments() {
        ContextLogger.info("배송 중인 목록 조회 요청");
        return shipmentRepository.findInTransit();
    }
    
    // 오늘 배송 완료된 목록 조회
    public List<Shipment> getDeliveredTodayShipments() {
        ContextLogger.info("오늘 배송 완료된 목록 조회 요청");
        return shipmentRepository.findDeliveredToday();
    }
    
    // 특정 상태의 배송 목록 조회
    public List<Shipment> getShipmentsByStatus(String status) {
        ContextLogger.info("상태별 배송 목록 조회 요청 - 상태: {}", status);
        return shipmentRepository.findByStatus(status);
    }
}