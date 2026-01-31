package com.example.rollback.service;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.domain.Order.OrderStatus;
import com.example.rollback.event.OrderFailed;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.util.GuidQueueUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 주문 비즈니스 로직 처리 (핵심 트랜잭션)
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orders;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher events;
    private final GuidQueueUtil guidQueueUtil;

    // 주문 생성 처리 - 트랜잭션 경계
    @Transactional
    public Order create(OrderRequest req) {
        try {
            // 0. 주문을 위한 고유 GUID 생성
            String guid = guidQueueUtil.getGUID();
            log.info("[GUID: {}] 고객 {}의 주문을 생성합니다.", guid, req.getCustomerName());
            
            // 1. 주문 데이터 저장 (아직 커밋 전)
            Order order = req.toOrder(guid);
            orders.save(order);
            log.info("[GUID: {}] 주문이 생성되었습니다. ID: {}", guid, order.getId());

            try {
                // 2. 외부 결제 API 호출
                paymentClient.pay(guid, order.getId(), req.getAmount(), req.isForcePaymentFailure());
                
                // 3. 주문 상태 업데이트
                order.markAsPaid();
                orders.updateStatus(order.getId(), OrderStatus.PAID.name());
                log.info("[GUID: {}] 주문 처리가 완료되었습니다.", guid);
                return order;
            } catch (Exception e) {
                // 4. 결제 실패 시 롤백 후 처리를 위한 이벤트 발행
                log.error("[GUID: {}] 주문 결제에 실패했습니다: {}", guid, e.getMessage());
                events.publishEvent(new OrderFailed(guid, order.getId(), e.getMessage()));
                // 5. 예외 재전달로 롤백 트리거 (핵심!)
                throw e;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("GUID 생성 중 인터럽트 발생: {}", e.getMessage());
            throw new RuntimeException("주문 생성에 실패했습니다.", e);
        }
    }
}