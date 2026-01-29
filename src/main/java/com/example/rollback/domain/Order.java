package com.example.rollback.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
public class Order {
    private Long id;
    private String customerName;
    private Integer amount;
    private String status;
    
    public Order(String customerName, Integer amount) {
        this.customerName = customerName;
        this.amount = amount;
        this.status = "CREATED";
        log.info("✨ Order domain object created for: {}", customerName);
    }
}