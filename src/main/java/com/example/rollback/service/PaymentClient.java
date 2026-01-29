package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/**
 * 외부 결제 시스템(PG)과의 연동을 시뮬레이션하는 클라이언트.
 * 실제 프로젝트에서는 이 부분에 실제 결제 API를 호출하는 로직이 들어갑니다.
 */
public class PaymentClient {

    /**
     * 주문에 대한 결제를 처리합니다.
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param forceFailure 결제 실패를 강제로 시뮬레이션할지 여부 (테스트용)
     */
    public void pay(Long orderId, Integer amount, boolean forceFailure) {
        log.info("Processing payment for order {} with amount {}", orderId, amount);
        log.info("💳 [PG] Connecting to external payment gateway...");

        if (forceFailure) {
            log.warn("Simulating payment failure for order {} {}", orderId , forceFailure);
            log.error("❌ [PG] Payment gateway rejected the request due to timeout simulation");
            // 테스트를 위해 의도적으로 예외를 발생시켜 결제 실패를 시뮬레이션합니다.
            throw new RuntimeException("Payment gateway error: Connection timeout");
        }

        log.info("✅ [PG] Payment approved by external gateway");
        log.info("Payment successful for order {}", orderId);
    }
}