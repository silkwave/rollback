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
public class OrderService {

    private final OrderRepository orders;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher events;

    @Transactional
    public Order create(OrderRequest req) {
        log.info("고객 {}의 주문을 생성합니다.", req.getCustomerName());
        Order order = req.toOrder();
        orders.save(order);
        log.info("주문이 생성되었습니다. ID: {}", order.getId());

        try {
            paymentClient.pay(order.getId(), req.getAmount(), req.isForcePaymentFailure());
            orders.updateStatus(order.getId(), "PAID");
            log.info("주문 {} 처리가 완료되었습니다.", order.getId());
            return order;
        } catch (Exception e) {
            log.error("주문 {} 결제에 실패했습니다: {}", order.getId(), e.getMessage());
            events.publishEvent(new OrderFailed(order.getId(), e.getMessage()));
            throw e;
        }
    }
}