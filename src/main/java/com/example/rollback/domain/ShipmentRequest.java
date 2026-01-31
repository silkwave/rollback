package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipmentRequest {
    
    @NotBlank(message = "배송지 주소는 필수입니다")
    private String shippingAddress;
}
