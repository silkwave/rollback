package com.example.rollback.service;

import com.example.rollback.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 외부 결제 시스템 클라이언트 (시뮬레이션)
@Slf4j
@Service
public class PaymentClient {

    // 결제 처리 메서드
    public void pay(Long orderId, Integer amount, boolean forceFailure) {
        log.info("\n\n\n\n=======================================================");
        log.info("주문 {} 결제 처리 중 (금액: {})", orderId, amount);

        // 테스트용 결제 실패 시뮬레이션
        if (forceFailure) {
            log.warn("주문 {} 결제 실패 시뮬레이션", orderId);
            throw new PaymentException("결제 게이트웨이 오류: 연결 시간 초과");
        }

        log.info("주문 {} 결제 성공", orderId);
    }
}