package com.example.rollback.service;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.event.OrderFailed;
import com.example.rollback.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
/** 주문 관련 비즈니스 로직을 처리하는 서비스 */
public class OrderService {

    private final OrderRepository orders;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher events;

    @Transactional
    // 주문 생성 및 결제 처리 메서드
    public Order create(OrderRequest req) {
        log.info("▶️▶️▶️▶️▶️ Starting order creation process for customer: {}", req.getCustomerName());
        // 1. 주문 정보 저장
        var order = orders.save(req.toOrder());
        log.info("▶️▶️▶️▶️▶️  Order created with ID: {}", order.getId());

        try {
            // 2. 결제 시도
            log.info("Attempting payment for order ID: {}", order.getId());
            paymentClient.pay(order.getId(), req.getAmount(), req.isForcePaymentFailure());
            log.info("✅ Payment successful for order ID: {}", order.getId());

            // 3. 결제 성공 시 상태 업데이트
            log.info("Updating order status to PAID for order ID: {}", order.getId());
            orders.updateStatus(order.getId(), "PAID");
            log.info("✅ Order status updated for order ID: {}", order.getId());
            log.info("✅ Order creation process completed successfully for order ID: {}", order.getId());
            return order;
        } catch (Exception e) {
            log.error("Payment failed for order {}: {}", order.getId(), e.getMessage());
            // 4. 결제 실패 시 이벤트 발행 및 예외 재발생 (롤백 유도)
            log.info("Publishing OrderFailed event for order ID: {}", order.getId());
            events.publishEvent(new OrderFailed(order.getId(), e.getMessage()));
            log.warn("❌ Order creation process failed. Transaction will be rolled back for order ID: {}", order.getId());
            throw e;
        }
    }
}