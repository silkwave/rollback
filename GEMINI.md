# 🔄 Spring Boot 트랜잭션 롤백 및 이벤트 처리 예제 분석

이 문서는 Spring Boot 환경에서 트랜잭션 실패 시 롤백을 처리하고, 롤백이 완료된 후에만 특정 로직(예: 실패 알림)을 실행하는 예제 프로젝트를 분석하고 설명합니다.

---

### 📌 전체 목표 요약

- **결제 실패 시나리오**: 주문 생성 -> DB 저장 -> 외부 결제 API 호출
- **트랜잭션 롤백**: 결제 실패 시, 이전에 저장된 주문 데이터를 DB에서 롤백(제거)합니다.
- **분리된 알림**: 주문 트랜잭션의 롤백이 **완전히 성공한 후에만** 별도의 트랜잭션에서 실패 알림을 기록하고 보냅니다.
- **결과 확인**: 프론트엔드 UI를 통해 주문 성공, 주문 실패, 롤백 결과, 실패 알림 기록을 시각적으로 확인합니다.

---

### 🧱 기술 스택

- **Backend**:
  - Java 21
  - Spring Boot 3.2.0
  - Gradle
  - MyBatis 3.0.3 (Data Persistence)
  - H2 (In-Memory Database)
  - Lombok & Slf4j
- **Frontend**:
  - HTML5
  - CSS3
  - Vanilla JavaScript

---

### 📂 프로젝트 구조

```
/
├── build.gradle
├── src/main/
│   ├── java/com/example/rollback/
│   │   ├── RollbackApplication.java         # Spring Boot 시작점
│   │   ├── controller/
│   │   │   └── OrderController.java         # 주문 관련 REST API 컨트롤러
│   │   ├── domain/
│   │   │   ├── Order.java                   # 주문 도메인 객체
│   │   │   ├── OrderRequest.java            # 주문 생성 요청 DTO
│   │   │   └── NotificationLog.java         # 알림 로그 도메인 객체
│   │   ├── event/
│   │   │   ├── OrderFailed.java             # 주문 실패 이벤트 객체
│   │   │   └── FailureHandler.java          # 주문 실패 이벤트를 처리하는 리스너
│   │   ├── repository/
│   │   │   ├── OrderRepository.java         # 주문 MyBatis Mapper
│   │   │   └── NotificationLogRepository.java # 알림 로그 MyBatis Mapper
│   │   └── service/
│   │       ├── OrderService.java            # 주문 비즈니스 로직 (핵심 트랜잭션)
│   │       ├── PaymentClient.java           # 외부 결제 시스템 호출 시뮬레이션
│   │       └── NotificationService.java     # 알림 전송 및 로그 기록 서비스
│   └── resources/
│       ├── application.yml                  # 애플리케이션 설정
│       ├── schema.sql                       # H2 데이터베이스 테이블 생성 스크립트
│       ├── mapper/
│       │   ├── OrderMapper.xml              # 주문 관련 SQL
│       │   └── NotificationLogMapper.xml    # 알림 로그 관련 SQL
│       └── static/
│           ├── index.html                   # 프론트엔드 UI
│           ├── script.js                    # 프론트엔드 로직
│           └── style.css                    # 프론트엔드 스타일
```

---

### ⚙️ 핵심 동작 원리

이 예제의 핵심은 Spring의 트랜잭션 관리와 이벤트 리스너를 결합하여 **"롤백 후 처리"** 로직을 구현하는 것입니다.

#### 1. 결제 성공 흐름

1.  **`OrderController`**: `/api/orders` (POST) 요청 수신
2.  **`OrderService.create`**: `@Transactional` 시작
3.  `orders.save(order)`: `orders` 테이블에 주문 데이터 **INSERT**
4.  `paymentClient.pay()`: 결제 시도 -> **성공**
5.  `orders.updateStatus()`: `orders` 테이블의 주문 상태를 'PAID'로 **UPDATE**
6.  **`OrderService.create`**: 트랜잭션 **COMMIT**
7.  **`OrderController`**: 클라이언트에 성공 응답 반환

#### 2. 결제 실패 및 롤백 흐름

1.  **`OrderController`**: `/api/orders` (POST) 요청 수신 (결제 실패 옵션 활성화)
2.  **`OrderService.create`**: `@Transactional` 시작
3.  `orders.save(order)`: `orders` 테이블에 주문 데이터 **INSERT** (아직 커밋되지 않은 상태)
4.  `paymentClient.pay()`: 결제 시도 -> **실패** (`RuntimeException` 발생)
5.  **`OrderService.create`** (catch 블록):
    - `events.publishEvent(new OrderFailed(...))`: `OrderFailed` 이벤트 발행
    - `throw e;`: 예외를 다시 던짐 -> Spring 트랜잭션 관리자에게 롤백이 필요함을 알림
6.  **Spring Transaction Manager**: 예외를 감지하고 트랜잭션을 **ROLLBACK** 하도록 마킹
7.  **트랜잭션 종료**: `OrderService.create` 메서드 종료 시점. 마킹된 트랜잭션이 **실제 롤백**됨. (3번에서 INSERT된 주문 데이터가 제거됨)
8.  **`FailureHandler.handle`**: `@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)`에 따라 롤백이 완료된 후 호출됨.
    - `notifier.sendFailure()`: `NotificationService`의 실패 알림 메서드 호출
9.  **`NotificationService.sendFailure`**: `@Transactional(propagation = Propagation.REQUIRES_NEW)`에 따라 **새로운 트랜잭션** 시작.
    - `notificationLogRepository.save()`: `notification_logs` 테이블에 실패 기록 **INSERT**
    - 새로운 트랜잭션 **COMMIT** (주문 트랜잭션과 무관하게 커밋됨)
10. **`OrderController`**: 클라이언트에 실패 응답 반환

---

### 🔍 주요 코드 분석

#### `OrderService.java` - 메인 트랜잭션

```java
@Transactional
public Order create(OrderRequest req) {
    // 1. 주문 데이터를 DB에 저장 (아직 커밋 전)
    Order order = req.toOrder();
    orders.save(order);

    try {
        // 2. 외부 결제 API 호출
        paymentClient.pay(order.getId(), req.getAmount(), req.isForcePaymentFailure());
        orders.updateStatus(order.getId(), "PAID");
        return order;
    } catch (Exception e) {
        // 3. 결제 실패 시
        // 3-1. 롤백 후 실행될 이벤트 발행
        events.publishEvent(new OrderFailed(order.getId(), e.getMessage()));
        // 3-2. 예외를 다시 던져서 트랜잭션 롤백 트리거
        throw e;
    }
}
```

- **`@Transactional`**: 이 메서드 전체가 하나의 트랜잭션으로 묶입니다.
- `throw e;`가 핵심입니다. 이 코드가 없으면 예외가 서비스 레벨에서 처리되고, Spring은 트랜잭션을 정상 커밋해버립니다.

#### `FailureHandler.java` - 롤백 후 이벤트 리스너

```java
@Component
@RequiredArgsConstructor
public class FailureHandler {

    private final NotificationService notifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async
    public void handle(OrderFailed event) {
        notifier.sendFailure(event.getOrderId(), event.getReason());
    }
}
```

- **`@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)`**: 이 리스너가 오직 트랜잭션이 **롤백된 후에만** 호출되도록 보장하는 가장 중요한 부분입니다.
- **`@Async`**: 이벤트를 비동기적으로 처리하여 API 응답 시간에 영향을 주지 않습니다. (`RollbackApplication`의 `@EnableAsync` 필요)

#### `NotificationService.java` - 별도 트랜잭션의 알림

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFailure(Long orderId, String reason) {
        String message = String.format("주문 %d 실패: %s - 고객에게 이메일 발송됨", orderId, reason);
        log.error(message);
        notificationLogRepository.save(new NotificationLog(orderId, message, "FAILURE"));
    }
}
```

- **`@Transactional(propagation = Propagation.REQUIRES_NEW)`**: `sendFailure`가 호출될 때, 기존 트랜잭션(이미 롤백된)에 참여하는 대신 항상 **새로운 트랜잭션**을 시작하도록 합니다. 이 덕분에 주문 롤백과 상관없이 실패 로그를 DB에 안정적으로 저장할 수 있습니다.

---

### 💾 데이터베이스

`schema.sql`에 의해 두 개의 테이블이 생성됩니다.

- **`orders`**: 주문 정보를 저장합니다. 결제 실패 시 이곳에 저장된 레코드가 롤백됩니다.
  - `id`, `customer_name`, `amount`, `status`
- **`notification_logs`**: 알림 성공/실패 로그를 저장합니다. `NotificationService`에 의해 별도 트랜잭션으로 데이터가 저장됩니다.
  - `id`, `order_id`, `message`, `type`, `created_at`

---

### 🔌 API 엔드포인트

- **`POST /api/orders`**: 새 주문을 생성합니다.
  - **Request Body**:
    ```json
    {
      "customerName": "홍길동",
      "amount": 15000,
      "forcePaymentFailure": false 
    }
    ```
    - `forcePaymentFailure`를 `true`로 설정하면 `PaymentClient`가 의도적으로 예외를 발생시킵니다.
- **`GET /api/orders`**: 모든 주문 목록을 조회합니다.
- **`GET /api/orders/{id}`**: 특정 ID의 주문을 조회합니다.

---

### 🖥️ 프론트엔드

`static/index.html`은 간단한 UI를 제공하여 백엔드 기능을 쉽게 테스트할 수 있도록 합니다.

- **주문 생성 폼**: 고객명, 금액을 입력하고 '결제 실패 강제 발생' 옵션을 선택하여 주문을 생성할 수 있습니다.
- **주문 목록**: 현재 `orders` 테이블에 저장된 모든 주문을 보여줍니다. 결제가 실패하고 롤백이 성공하면, 실패한 주문은 이 목록에 나타나지 않습니다.
- **실행 로그**: 프론트엔드에서 API를 호출하고 응답받는 과정을 실시간으로 보여줍니다. 이를 통해 성공/실패 여부와 롤백 메시지를 직관적으로 확인할 수 있습니다.