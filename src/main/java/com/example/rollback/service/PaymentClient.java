package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentClient {
    
    public void pay(Long orderId, Integer amount, boolean forceFailure) {
        log.info("Processing payment for order {} with amount {}", orderId, amount);
        
        if (forceFailure) {
            log.warn("Simulating payment failure for order {}", orderId);
            throw new RuntimeException("Payment gateway error: Connection timeout");
        }
        
        log.info("Payment successful for order {}", orderId);
    }
}