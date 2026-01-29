📌 전체 목표 요약

Spring Boot + H2 + MyBatis 환경에서

결제 실패 시 트랜잭션을 롤백

롤백 이후에만 실패 알림 로직 실행

프론트엔드(HTML + JS)에서 롤백 결과를 눈으로 확인

하는 예제를 구현한다.

🧱 기술 스택
백엔드

JDK 21
Gradle
Spring Boot 3.x
@Slf4j
H2 Database (In-Memory)
MyBatis
Spring Web
Spring Transaction
프론트엔드
index.html
style.css
script.js
Vanilla JavaScript (프레임워크 없음)

rollback/
├── build.gradle
├── src/main/
│   ├── java/com/example/rollback/
│   │   ├── RollbackApplication.java
│   │   ├── domain/
│   │   │   ├── Order.java
│   │   │   └── OrderRequest.java
│   │   ├── repository/
│   │   │   └── OrderRepository.java   (MyBatis Mapper)
│   │   ├── service/
│   │   │   ├── OrderService.java
│   │   │   ├── PaymentClient.java
│   │   │   └── NotificationService.java
│   │   ├── event/
│   │   │   ├── OrderFailed.java
│   │   │   └── FailureHandler.java
│   │   └── controller/
│   │       └── OrderController.java
│   └── resources/
│       ├── application.yml
│       └── static/
│           ├── index.html
│           ├── style.css
│           └── script.js

pay() 예외 발생
→ catch
→ OrderFailed 이벤트 발행
→ 예외 재던짐
→ 트랜잭션 rollback-only 마킹
→ 트랜잭션 종료 시점
→ DB ROLLBACK
→ AFTER_ROLLBACK 이벤트 리스너 실행
→ notifier.sendFailure()

@Service
@RequiredArgsConstructor
class OrderService {

    private final OrderRepository orders;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher events;

    @Transactional
    public void create(OrderRequest req) {
        var order = orders.save(req.toOrder());

        try {
            paymentClient.pay(order.id(), req.amount());
        } catch (Exception e) {
            // 결제 실패 이벤트 발행
            events.publishEvent(new OrderFailed(order.id(), e.getMessage()));
            // 예외 재던져 트랜잭션 롤백 유도
            throw e;
        }
    }
}


public class OrderFailed {

    private final Long orderId;
    private final String reason;

    public OrderFailed(Long orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }
}


@Component
@RequiredArgsConstructor
class FailureHandler {

    private final NotificationService notifier;

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_ROLLBACK
    )
    void handle(OrderFailed event) {
        notifier.sendFailure(
            event.getOrderId(),
            event.getReason()
        );
    }
}
