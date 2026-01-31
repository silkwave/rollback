package com.example.rollback.event;

import com.example.rollback.util.CtxMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// 주문 실패 이벤트 객체
@Slf4j
@Data
public class OrderFailed {

    private final String guid;
    private final Long orderId;
    private final String reason;
    private final CtxMap context;

    // 실패 이벤트 생성 (하위 호환성)
    public OrderFailed(String guid, Long orderId, String reason) {
        this.guid = guid;
        this.orderId = orderId;
        this.reason = reason;
        this.context = CtxMap.empty();
        
        log.info("\n\n\n\n=======================================================");           
        log.info("[GUID: {}] 주문 ID {}에 대한 OrderFailed 이벤트가 생성되었습니다.", guid, orderId);
    }

    // 실패 이벤트 생성 (컨텍스트 포함)
    public OrderFailed(CtxMap context, Long orderId, String reason) {
        this.guid = context.getString("guid", "unknown");
        this.orderId = orderId;
        this.reason = reason;
        this.context = context;
        
        log.info("\n\n\n\n=======================================================");           
        log.info("[GUID: {}] 주문 ID {}에 대한 OrderFailed 이벤트가 생성되었습니다. 컨텍스트: {}", 
                this.guid, orderId, context.asReadOnlyMap());
    }
}