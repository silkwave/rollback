package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 배송 엔티티
@Slf4j
@Data
public class Shipment {
    private Long id;
    private Long orderId;
    private String trackingNumber;
    private String carrier;
    private ShipmentStatus status = ShipmentStatus.PREPARING;
    private String shippingAddress;
    private LocalDate estimatedDelivery;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 배송 상태 열거형
    public enum ShipmentStatus {
        PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, CANCELLED
    }
    
    // 배송 준비 완료
    public Shipment markAsPrepared() {
        this.status = ShipmentStatus.PREPARING;
        this.updatedAt = LocalDateTime.now();
        log.info("배송 준비 완료 - 주문 ID: {}, 배송 ID: {}", orderId, id);
        return this;
    }
    
    // 배송 시작
    public Shipment markAsShipped(String trackingNumber, String carrier) {
        this.status = ShipmentStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shippedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // 예상 배송일은 3일 후로 설정
        this.estimatedDelivery = LocalDate.now().plusDays(3);
        log.info("배송 시작 - 주문 ID: {}, 배송 ID: {}, 운송장번호: {}", orderId, id, trackingNumber);
        return this;
    }
    
    // 배송 중 상태로 변경
    public Shipment markAsInTransit() {
        this.status = ShipmentStatus.IN_TRANSIT;
        this.updatedAt = LocalDateTime.now();
        log.info("배송 중 - 주문 ID: {}, 배송 ID: {}", orderId, id);
        return this;
    }
    
    // 배송 완료
    public Shipment markAsDelivered() {
        this.status = ShipmentStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        log.info("배송 완료 - 주문 ID: {}, 배송 ID: {}", orderId, id);
        return this;
    }
    
    // 배송 취소
    public Shipment markAsCancelled() {
        this.status = ShipmentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        log.info("배송 취소 - 주문 ID: {}, 배송 ID: {}", orderId, id);
        return this;
    }
    
    // 배송 상태 확인
    public boolean isPrepared() {
        return ShipmentStatus.PREPARING.equals(this.status);
    }
    
    public boolean isShipped() {
        return ShipmentStatus.SHIPPED.equals(this.status);
    }
    
    public boolean isInTransit() {
        return ShipmentStatus.IN_TRANSIT.equals(this.status);
    }
    
    public boolean isDelivered() {
        return ShipmentStatus.DELIVERED.equals(this.status);
    }
    
    public boolean isCancelled() {
        return ShipmentStatus.CANCELLED.equals(this.status);
    }
    
    // 배송 데이터 유효성 검사
    public static void validateShipmentData(Long orderId, String shippingAddress) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID는 필수입니다");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("배송지 주소는 필수입니다");
        }
    }
    
    // 배송 생성 팩토리 메서드
    public static Shipment create(Long orderId, String shippingAddress) {
        validateShipmentData(orderId, shippingAddress);
        Shipment shipment = new Shipment();
        shipment.orderId = orderId;
        shipment.shippingAddress = shippingAddress;
        shipment.status = ShipmentStatus.PREPARING;
        shipment.createdAt = LocalDateTime.now();
        shipment.updatedAt = LocalDateTime.now();
        log.info("배송 객체 생성됨 - 주문 ID: {}, 배송지: {}", orderId, shippingAddress);
        return shipment;
    }
    
    // 운송장번호 생성
    public static String generateTrackingNumber(String carrier) {
        String carrierCode = getCarrierCode(carrier);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("%s-%s-%s", carrierCode, timestamp, random);
    }
    
    // 운송사 코드 가져오기
    private static String getCarrierCode(String carrier) {
        if (carrier == null) return "UNK";
        
        switch (carrier.toLowerCase()) {
            case "cj대한통운":
            case "cj":
                return "CJ";
            case "롯데택배":
            case "lotte":
                return "LT";
            case "우체국":
            case "post":
                return "PO";
            case "한진택배":
            case "hanjin":
                return "HJ";
            default:
                return carrier.toUpperCase().substring(0, Math.min(2, carrier.length()));
        }
    }
}