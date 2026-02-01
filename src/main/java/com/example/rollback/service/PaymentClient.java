package com.example.rollback.service;

import com.example.rollback.exception.PaymentException;
import com.example.rollback.retry.LockRetryTemplate;
import com.example.rollback.retry.LinearBackoffRetryStrategy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// 외부 결제 시스템 클라이언트 (시뮬레이션)
@Service
public class PaymentClient {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);
    
    private final LockRetryTemplate retryTemplate;
    
    public PaymentClient(LockRetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    // 결제 처리 메서드
    public void pay(String guid, Long orderId, Integer amount, boolean forceFailure) {
        log.info("\n\n\n\n=======================================================");
        
        retryTemplate.execute(() -> {
            log.info("[GUID: {}] 주문 {} 결제 처리 중 (금액: {})", guid, orderId, amount);
            
            // 테스트용 결제 실패 시뮬레이션
            if (forceFailure) {
                log.warn("[GUID: {}] 주문 {} 결제 실패 시뮬레이션", guid, orderId);
                throw new PaymentException("결제 게이트웨이 오류: 연결 시간 초과");
            }
            
            log.info("[GUID: {}] 주문 {} 결제 성공", guid, orderId);
            return null; // void 메서드를 위한 null 반환
        });
    }
}