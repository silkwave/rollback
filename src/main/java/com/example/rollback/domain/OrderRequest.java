package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class OrderRequest {
    @NotBlank(message = "고객 이름은 필수입니다")
    private String customerName;
    
    @NotNull(message = "금액은 필수입니다")
    private Integer amount;
    
    private boolean forcePaymentFailure = false;
    
    public Order toOrder() {
        log.info("➡️ 주문 요청을 주문 객체로 변환 중 (고객명: {})", this.customerName);
        return new Order(this.customerName, this.amount);
    }
}