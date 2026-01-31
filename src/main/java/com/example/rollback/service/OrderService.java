package com.example.rollback.service;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.domain.Order.OrderStatus;
import com.example.rollback.event.OrderFailed;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.ContextLogger;
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
        long startTime = System.currentTimeMillis();
        
        try {
            // 컨텍스트에서 GUID 가져오기 (Controller에서 이미 설정됨)
            String guid = ContextHolder.getCurrentGuid();
            
            ContextLogger.debug("1. GUID 확인: {}", guid);
            ContextLogger.debug("2. OrderRequest 확인: {}", req);
            
            // null 체크 추가
            if (req == null) {
                ContextLogger.error("OrderRequest가 null입니다");
                throw new IllegalArgumentException("OrderRequest가 null입니다");
            }
            
            // 비즈니스 정보를 컨텍스트에 추가
            String customerName = req.getCustomerName();
            Integer amount = req.getAmount();
            boolean forceFailure = req.isForcePaymentFailure();
            
            ContextLogger.debug("3. 추출된 값 - 고객: {}, 금액: {}, 실패유무: {}", customerName, amount, forceFailure);
            
            ContextLogger.logOrderStart(customerName, amount);
            ContextLogger.logStep("ORDER_CREATE", "주문 데이터 저장 시작");
            
            // 1. 주문 데이터 저장 (아직 커밋 전)
            Order order = req.toOrder(guid);
            ContextLogger.debug("4. 생성된 Order: {}", order);
            
            orders.save(order);
            ContextLogger.debug("5. 저장 완료");
            
            // 컨텍스트에 주문 ID 업데이트
            Long orderId = order.getId();
            ContextLogger.debug("6. 주문 ID: {}", orderId);
            ContextHolder.put("orderId", orderId);
            ContextLogger.logOrderCreated(orderId);
            ContextLogger.logStep("ORDER_CREATE", "주문 데이터 저장 완료");

            try {
                ContextLogger.logStep("PAYMENT", "결제 처리 시작");
                ContextLogger.logPaymentStart(orderId, amount);
                
                // 2. 외부 결제 API 호출
                paymentClient.pay(guid, orderId, amount, forceFailure);
                ContextLogger.debug("7. 결제 성공");
                
                // 3. 주문 상태 업데이트
                order.markAsPaid();
                orders.updateStatus(orderId, OrderStatus.PAID.name());
                ContextLogger.debug("8. 상태 업데이트 완료");
                
                ContextLogger.logPaymentSuccess(orderId);
                ContextLogger.logStep("ORDER_COMPLETE", "주문 처리 완료");
                
                // 처리 결과 저장
                long duration = System.currentTimeMillis() - startTime;
                ContextLogger.logPerformance("주문 처리", duration);
                ContextHolder.addProcessingResult("SUCCESS", "주문이 성공적으로 처리되었습니다");
                
                return order;
                
            } catch (Exception e) {
                // 4. 결제 실패 시 롤백 후 처리를 위한 이벤트 발행
                ContextLogger.logPaymentFailure(orderId, e.getMessage(), e);
                ContextLogger.logStep("PAYMENT_FAILED", "결제 실패로 인한 롤백");
                ContextHolder.addProcessingResult("FAILED", "결제 실패: " + e.getMessage());
                
                // 컨텍스트와 함께 이벤트 발행
                events.publishEvent(new OrderFailed(ContextHolder.copyContext(), orderId, e.getMessage()));
                
                // 5. 예외 재전달로 롤백 트리거 (핵심!)
                throw e;
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                ContextLogger.error("GUID 생성 중 인터럽트 발생: {}", e.getMessage(), e);
            } else {
                ContextLogger.error("주문 생성 중 예외 발생: {}", e.getMessage(), e);
                e.printStackTrace();
            }
            
            // 처리 결과 저장
            long duration = System.currentTimeMillis() - startTime;
            ContextLogger.logPerformance("주문 처리 (실패)", duration);
            ContextHolder.addProcessingResult("ERROR", e.getMessage());
            
            throw new RuntimeException("주문 생성에 실패했습니다.", e);
        }
    }
}