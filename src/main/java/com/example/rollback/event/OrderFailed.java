package com.example.rollback.event;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderFailed {

    private final Long orderId;
    private final String reason;

    public OrderFailed(Long orderId, String reason) {
        log.info("🔥 OrderFailed event created for order ID: {}", orderId);
        this.orderId = orderId;
        this.reason = reason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }
}