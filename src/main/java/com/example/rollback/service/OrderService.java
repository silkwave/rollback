package com.example.rollback.service;

import com.example.rollback.domain.Order;
import com.example.rollback.domain.OrderRequest;
import com.example.rollback.domain.Order.OrderStatus;
import com.example.rollback.event.OrderFailed;
import com.example.rollback.repository.OrderRepository;
import com.example.rollback.util.ContextHolder;
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
    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;
    // 주문 생성 처리 - 트랜잭션 경계
    @Transactional
    public Order create(OrderRequest req) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid(); // 메서드 시작 시 바로 할당
        
        try {
            // 컨텍스트에서 GUID 가져오기 (Controller에서 이미 설정됨)
            
            log.debug("[GUID: {}] OrderService.create - OrderRequest: {}", guid, req);
            
            // null 체크 추가
            if (req == null) {
                log.error("[GUID: {}] OrderRequest가 null입니다", guid);
                throw new IllegalArgumentException("OrderRequest가 null입니다");
            }
            
            // 비즈니스 정보를 컨텍스트에 추가
            String customerName = req.getCustomerName();
            String productName = req.getProductName();
            Integer quantity = req.getQuantity();
            Integer amount = req.getAmount();
            boolean forceFailure = req.isForcePaymentFailure();
            
            log.debug("[GUID: {}] 추출된 값 - 고객: {}, 상품: {}, 수량: {}, 금액: {}, 실패유무: {}", 
                guid, customerName, productName, quantity, amount, forceFailure);
            
            
            log.info("[GUID: {}] 주문 처리를 시작합니다 - 고객: {}, 금액: {}", guid, customerName, amount);
            log.info("[GUID: {}] 주문 상세 정보 - 상품: {}, 수량: {}", guid, productName, quantity);
            log.info("[GUID: {}] [ORDER_CREATE] 주문 데이터 저장 시작", guid);
            
            // 1. 재고 확인 및 예약
            log.info("[GUID: {}] [INVENTORY_CHECK] 재고 확인 시작", guid);
            if (!inventoryService.hasEnoughStock(productName, quantity)) {
                throw new IllegalStateException("재고가 부족합니다: " + productName);
            }
            
            inventoryService.reserveStock(productName, quantity);
            log.info("[GUID: {}] [INVENTORY_CHECK] 재고 예약 완료", guid);
            
            // 2. 주문 데이터 저장 (아직 커밋 전)
            Order order = req.toOrder(guid);
            log.debug("[GUID: {}] 4. 생성된 Order: {}", guid, order);
            
            orders.save(order);
            log.debug("[GUID: {}] 5. 저장 완료", guid);
            
            // 컨텍스트에 주문 ID 업데이트
            Long orderId = order.getId();
            log.debug("[GUID: {}] 6. 주문 ID: {}", guid, orderId);
            ContextHolder.put("orderId", orderId);
            log.info("[GUID: {}] 주문이 성공적으로 생성되었습니다 - 주문 ID: {}", guid, orderId);
            log.info("[GUID: {}] [ORDER_CREATE] 주문 데이터 저장 완료", guid);

            try {
                log.info("[GUID: {}] [PAYMENT] 결제 처리 시작", guid);
                log.info("[GUID: {}] 결제 처리를 시작합니다 - 주문 ID: {}, 금액: {}", guid, orderId, amount);
                
                // 3. 외부 결제 API 호출
                paymentClient.pay(guid, orderId, amount, forceFailure);
                log.debug("[GUID: {}] 7. 결제 성공", guid);
                
                // 4. 주문 상태 업데이트
                order.markAsPaid();
                orders.updateStatus(orderId, OrderStatus.PAID.name());
                log.debug("[GUID: {}] 8. 상태 업데이트 완료", guid);
                
                // 5. 재고 실제 차감
                inventoryService.deductStock(productName, quantity);
                log.info("[GUID: {}] [INVENTORY_DEDUCT] 재고 실제 차감 완료", guid);
                
                
                log.info("[GUID: {}] 결제가 성공적으로 완료되었습니다 - 주문 ID: {}", guid, orderId);
                log.info("[GUID: {}] [ORDER_COMPLETE] 주문 처리 완료", guid);
                
                // 처리 결과 저장
                long duration = System.currentTimeMillis() - startTime;
                log.info("[GUID: {}] 성능: {} - 소요시간: {}ms", guid, "주문 처리", duration);
                ContextHolder.addProcessingResult("SUCCESS", "주문이 성공적으로 처리되었습니다");
                
                return order;
                
            } catch (Exception e) {
                // 4. 결제 실패 시 롤백 후 처리를 위한 이벤트 발행
                // 4. 결제 실패 시 롤백 후 처리를 위한 이벤트 발행
                log.error("[GUID: {}] 결제에 실패했습니다 - 주문 ID: {}, 사유: {}", guid, orderId, e.getMessage(), e);
                log.info("[GUID: {}] [PAYMENT_FAILED] 결제 실패로 인한 롤백", guid);
                ContextHolder.addProcessingResult("FAILED", "결제 실패: " + e.getMessage());
                
                // 컨텍스트와 함께 이벤트 발행
                events.publishEvent(new OrderFailed(ContextHolder.copyContext(), orderId, e.getMessage()));
                
                // 5. 예외 재전달로 롤백 트리거 (핵심!)
                throw e;
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                log.error("[GUID: {}] GUID 생성 중 인터럽트 발생: {}", guid, e.getMessage(), e);
            } else {
                log.error("[GUID: {}] 주문 생성 중 예외 발생: {}", guid, e.getMessage(), e);
                e.printStackTrace();
            }
            
            // 처리 결과 저장
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GUID: {}] 성능: {} - 소요시간: {}ms", guid, "주문 처리 (실패)", duration);
            ContextHolder.addProcessingResult("ERROR", e.getMessage());
            
            throw new RuntimeException("주문 생성에 실패했습니다.", e);
        }
    }
    
    // 주문 수정 (트랜잭션)
    @Transactional
    public Order update(Long id, OrderRequest req) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid(); // 여기서 선언
        
        try {
            log.info("[GUID: {}] 주문 수정 시작 - ID: {}", guid, id);
            
            // 기존 주문 정보 가져오기
            Order existingOrder = orders.findById(id);
            if (existingOrder == null) {
                throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + id);
            }

            // 주문 상태 확인 (특정 상태에서는 수정 불가)
            if (existingOrder.getStatus() == OrderStatus.DELIVERED || 
                existingOrder.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("배송완료되거나 취소된 주문은 수정할 수 없습니다: " + id);
            }
            
            // 기존 재고 정보
            String oldProductName = existingOrder.getProductName();
            Integer oldQuantity = existingOrder.getQuantity();

            // 요청된 재고 정보
            String newProductName = req.getProductName();
            Integer newQuantity = req.getQuantity();

            // 1. 상품명 변경 처리
            if (!oldProductName.equals(newProductName)) {
                // 이전 상품의 재고 예약 해제
                inventoryService.releaseReservation(oldProductName, oldQuantity);
                log.info("[GUID: {}] 이전 상품 재고 예약 해제 완료 - 상품: {}, 수량: {}", guid, oldProductName, oldQuantity);
                
                // 새 상품 재고 예약
                inventoryService.reserveStock(newProductName, newQuantity);
                log.info("[GUID: {}] 새 상품 재고 예약 완료 - 상품: {}, 수량: {}", guid, newProductName, newQuantity);
            } 
            // 2. 상품명은 동일하지만 수량 변경 처리
            else if (!oldQuantity.equals(newQuantity)) {
                int quantityDifference = newQuantity - oldQuantity;
                if (quantityDifference > 0) { // 수량 증가
                    inventoryService.reserveStock(newProductName, quantityDifference);
                    log.info("[GUID: {}] 상품 수량 증가로 추가 재고 예약 완료 - 상품: {}, 수량: {}", guid, newProductName, quantityDifference);
                } else { // 수량 감소
                    inventoryService.releaseReservation(newProductName, Math.abs(quantityDifference));
                    log.info("[GUID: {}] 상품 수량 감소로 재고 예약 해제 완료 - 상품: {}, 수량: {}", guid, newProductName, Math.abs(quantityDifference));
                }
            }
            
            // 주문 정보 업데이트
            existingOrder.setCustomerName(req.getCustomerName());
            existingOrder.setAmount(req.getAmount());
            existingOrder.setProductName(newProductName); // DTO에서 가져온 새 상품명으로 업데이트
            existingOrder.setQuantity(newQuantity);     // DTO에서 가져온 새 수량으로 업데이트
            
            orders.update(existingOrder);
            log.info("[GUID: {}] 주문 수정 완료 - ID: {}", guid, id);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GUID: {}] 성능: {} - 소요시간: {}ms", guid, "주문 수정", duration);
            
            return existingOrder;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GUID: {}] 성능: {} - 소요시간: {}ms", guid, "주문 수정 (실패)", duration);
            log.error("[GUID: {}] 주문 수정 중 예외 발생: {}", guid, e.getMessage(), e);
            throw new RuntimeException("주문 수정에 실패했습니다.", e);
        }
    }
    
    // 주문 취소 (트랜잭션)
    @Transactional
    public void cancel(Long id) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid(); // 여기서 선언
        
        try {
            log.info("[GUID: {}] 주문 취소 시작 - ID: {}", guid, id);
            
            Order order = orders.findById(id);
            if (order == null) {
                throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + id);
            }
            
            // 주문 상태 확인
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("이미 취소된 주문입니다: " + id);
            }
            
            if (order.getStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("배송완료된 주문은 취소할 수 없습니다: " + id);
            }
            
            // 재고 복원 (결제 완료, 배송 준비중, 배송 중 상태에서만)
            if (order.getStatus() == OrderStatus.PAID || 
                order.getStatus() == OrderStatus.PREPARING || 
                order.getStatus() == OrderStatus.SHIPPED) {
                
                inventoryService.releaseReservation(order.getProductName(), order.getQuantity());
                log.info("[GUID: {}] 재고 예약 해제 완료 - 상품: {}, 수량: {}", 
                    guid, order.getProductName(), order.getQuantity());
            }
            
            // 배송 취소 (배송이 시작된 경우)
            if (order.getStatus() == OrderStatus.SHIPPED) {
                shipmentService.findByOrderId(id).ifPresent(shipment -> {
                    try {
                        shipmentService.cancelShipment(shipment.getId());
                        log.info("[GUID: {}] 배송 취소 완료 - 배송 ID: {}", guid, shipment.getId());
                    } catch (Exception e) {
                        log.warn("[GUID: {}] 배송 취소 실패 - 배송 ID: {}, 사유: {}", 
                            guid, shipment.getId(), e.getMessage());
                    }
                });
            }
            
            // 주문 상태를 취소로 변경
            orders.updateStatus(id, OrderStatus.CANCELLED.name());
            log.info("[GUID: {}] 주문 취소 완료 - ID: {}", guid, id);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GUID: {}] 성능: {} - 소요시간: {}ms", guid, "주문 취소", duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GUID: {}] 성능: {} - 소요시간: {}ms", guid, "주문 취소 (실패)", duration);
            log.error("[GUID: {}] 주문 취소 중 예외 발생: {}", guid, e.getMessage(), e);
            throw new RuntimeException("주문 취소에 실패했습니다.", e);
        }
    }
}