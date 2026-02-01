package com.example.rollback.service;

import com.example.rollback.exception.PaymentException;
import com.example.rollback.retry.LockRetryTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// 외부 결제 시스템 클라이언트 (시뮬레이션)
@Slf4j
@Service
public class PaymentClient {
   
    private final LockRetryTemplate retryTemplate;
    
    public PaymentClient(LockRetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    // 결제 처리 메서드 (주문용 - 하위 호환성)
    public void pay(String guid, Long orderId, Integer amount, boolean forceFailure) {
        processPayment(guid, orderId, new BigDecimal(amount), forceFailure);
    }

    // 결제 처리 메서드 (은행용)
    public void processPayment(String guid, Long transactionId, BigDecimal amount, boolean forceFailure) {
        log.info("\n\n\n\n=======================================================");
        
        retryTemplate.execute(() -> {
            log.info("[GUID: {}] 거래 {} 결제 처리 중 (금액: {})", guid, transactionId, amount);
            
            // 테스트용 결제 실패 시뮬레이션
            if (forceFailure) {
                log.warn("[GUID: {}] 거래 {} 결제 실패 시뮬레이션", guid, transactionId);
                throw new PaymentException("결제 게이트웨이 오류: 연결 시간 초과");
            }
            
            log.info("[GUID: {}] 거래 {} 결제 성공", guid, transactionId);
            return null; // void 메서드를 위한 null 반환
        });
    }
}