package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class OrderRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotNull(message = "Amount is required")
    private Integer amount;
    
    private boolean forcePaymentFailure = false;
    
    public Order toOrder() {
        log.info("➡️ Converting OrderRequest to Order for customer: {}", this.customerName);
        return new Order(this.customerName, this.amount);
    }
}