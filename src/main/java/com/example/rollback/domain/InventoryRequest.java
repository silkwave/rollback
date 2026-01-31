package com.example.rollback.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {
    
    @NotBlank(message = "상품 이름은 필수입니다")
    private String productName;
    
    @NotNull(message = "현재 재고는 필수입니다")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    private Integer currentStock;
    
    @Min(value = 0, message = "최소 재고 수준은 0 이상이어야 합니다")
    private Integer minStockLevel = 10;
}
