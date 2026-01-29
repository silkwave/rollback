package com.example.rollback.event;

public class OrderFailed {

    private final Long orderId;
    private final String reason;

    public OrderFailed(Long orderId, String reason) {
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