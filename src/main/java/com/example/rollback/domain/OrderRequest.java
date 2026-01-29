package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotNull(message = "Amount is required")
    private Integer amount;
    
    private boolean forcePaymentFailure = false;
    
    public Order toOrder() {
        return new Order(this.customerName, this.amount);
    }
}