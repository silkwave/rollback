package com.example.rollback.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// 주문 생성 요청 DTO
@Slf4j
@Data
public class OrderRequest {
    
    @NotBlank(message = "고객 이름은 필수입니다")
    private String customerName;
    
    @NotNull(message = "금액은 필수입니다")
    @Min(value = 1, message = "금액은 1 이상이어야 합니다")
    private Integer amount;
    
    // 결제 실패 강제 여부 (테스트용)
    private boolean forcePaymentFailure = false;
    
    // public getters
    public String getCustomerName() {
        return customerName;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public boolean isForcePaymentFailure() {
        return forcePaymentFailure;
    }
    
    // 주문 객체로 변환
    public Order toOrder(String guid) {
        log.info("[GUID: {}] 주문 요청을 주문 객체로 변환 중 - 고객명: {}", guid, this.customerName);
        return Order.create(guid, this.customerName, this.amount);
    }
}