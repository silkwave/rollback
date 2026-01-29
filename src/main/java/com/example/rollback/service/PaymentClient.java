package com.example.rollback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentClient {

    public void pay(Long orderId, Integer amount, boolean forceFailure) {
        log.info("주문 {} 결제 처리 중 (금액: {})", orderId, amount);

        if (forceFailure) {
            log.warn("주문 {} 결제 실패 시뮬레이션", orderId);
            throw new RuntimeException("결제 게이트웨이 오류: 연결 시간 초과");
        }

        log.info("주문 {} 결제 성공", orderId);
    }
}