package com.example.rollback.service;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.domain.Order.OrderStatus;
import com.example.rollback.event.OrderFailed;
import com.example.rollback.repository.OrderRepository;
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

    // 주문 생성 처리 - 트랜잭션 경계
    @Transactional
    public Order create(OrderRequest req) {
        log.info("고객 {}의 주문을 생성합니다.", req.getCustomerName());
        
        // 1. 주문 데이터 저장 (아직 커밋 전)
        Order order = req.toOrder();
        orders.save(order);
        log.info("주문이 생성되었습니다. ID: {}", order.getId());

        try {
            // 2. 외부 결제 API 호출
            paymentClient.pay(order.getId(), req.getAmount(), req.isForcePaymentFailure());
            
            // 3. 주문 상태 업데이트
            order.markAsPaid();
            orders.updateStatus(order.getId(), OrderStatus.PAID.name());
            log.info("주문 {} 처리가 완료되었습니다.", order.getId());
            return order;
        } catch (Exception e) {
            // 4. 결제 실패 시 롤백 후 처리를 위한 이벤트 발행
            log.error("주문 {} 결제에 실패했습니다: {}", order.getId(), e.getMessage());
            events.publishEvent(new OrderFailed(order.getId(), e.getMessage()));
            // 5. 예외 재전달로 롤백 트리거 (핵심!)
            throw e;
        }
    }
}