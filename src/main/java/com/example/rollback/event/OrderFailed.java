package com.example.rollback.event;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// 주문 실패 이벤트 객체
@Slf4j
@Data
public class OrderFailed {

    private final Long orderId;
    private final String reason;

    // 실패 이벤트 생성
    public OrderFailed(Long orderId, String reason) {
        log.info("\n\n\n\n=======================================================");           
        log.info("주문 ID {}에 대한 OrderFailed 이벤트가 생성되었습니다.", orderId);
        this.orderId = orderId;
        this.reason = reason;
    }
}