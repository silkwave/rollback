package com.example.rollback.event;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderFailed {

    private final Long orderId;
    private final String reason;

    public OrderFailed(Long orderId, String reason) {
        log.info("🔥 주문 ID {}에 대한 OrderFailed 이벤트가 생성되었습니다.", orderId);
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