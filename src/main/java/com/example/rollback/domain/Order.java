package com.example.rollback.domain;

import lombok.Data;

@Data
public class Order {
    private Long id;
    private String customerName;
    private Integer amount;
    private String status;
    
    public Order(String customerName, Integer amount) {
        this.customerName = customerName;
        this.amount = amount;
        this.status = "CREATED";
    }
}