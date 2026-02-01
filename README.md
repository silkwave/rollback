# 🔄 Spring Boot 트랜잭션 롤백 및 이벤트 처리 예제

결제 실패 시나리오에서 트랜잭션 롤백 처리와 롤백 완료 후 이벤트 처리를 보여주는 Spring Boot 애플리케이션입니다.

## 📋 개요

이 프로젝트는 전자상거래 주문 처리 시스템의 완전한 라이프사이클을 구현합니다. 핵심 기능들은 다음과 같습니다:

**주요 패턴들:**
- **롤백 후 알림**: 결제 실패 시 트랜잭션 롤백과 롤백 완료 후 실패 알림
- **재시도 메커니즘**: 선형 증가 백오프 전략을 사용한 결제 재시도
- **재고 관리**: 주문 시 재고 예약, 결제 완료 시 재고 차감, 취소 시 재고 복원
- **주문 상태 관리**: CREATED → PAID → PREPARING → SHIPPED → DELIVERED
- **배송 추적**: 운송장번호 생성, 배송 상태 추적, 배송 완료 처리

**전체 처리 흐름:**
1. 주문 생성 (재고 확인 및 예약)
2. 결제 처리
3. 주문 상태 변경 및 재고 차감
4. 배송 생성 및 시작
5. 배송 완료 처리
6. 주문 취소 시 재고 복원 및 배송 취소

## 🎯 학습 목표

- **트랜잭션 관리**: Spring의 `@Transactional` 경계 이해
- **이벤트 기반 아키텍처**: 롤백 후 처리를 위한 `@TransactionalEventListener` 사용
- **재시도 패턴**: 전략 패턴을 사용한 재시도 로직 구현
- **선형 백오프 전략**: 대기 시간을 점진적으로 증가시키는 재시도 기법
- **트랜잭션 전파**: 독립적 트랜잭션을 위한 `REQUIRES_NEW`
- **재고 관리 시스템**: 재고 예약, 차감, 복원 패턴
- **주문 상태 머신**: 주문 라이프사이클 관리
- **배송 추적 시스템**: 운송장번호, 배송 상태 관리
- **에러 처리**: 롤백을 트리거하는 올바른 예외 처리
- **비동기 처리**: 블로킹 없는 실패 알림
- **REST API 설계**: CRUD operations와 상태 관리 API

## 🏗️ 아키텍처

```
 ┌─────────────┐    ┌──────────────┐    ┌─────────────────┐
│ Controller  │───▶│ OrderService │───▶│ PaymentClient   │
│             │    │              │    │ (Retry Logic)   │
└─────────────┘    └──────┬───────┘    └──────┬───────────┘
                           │                   │
         ┌──────────────────┼──────────────────┼──────────────────┐
         │                  │                  │                  │
         ▼                  ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ InventorySrv │   │ Repository   │   │ ShipmentSrv │   │RetryTemplate│
│              │   │ (Orders)     │   │              │   │(Strategy)   │
└──────┬───────┘   └──────────────┘   └──────┬───────┘   └──────────────┘
        │                                   │
        ▼                                   ▼ (롤백 후)
┌──────────────┐                   ┌──────────────┐    ┌─────────────────┐
│ Inventory   │                   │ Event        │    │ NotificationSvc │
│ Repository  │                   │ Publisher    │    │ (New Transaction)│
└──────────────┘                   └──────┬───────┘    └─────────────────┘
                                         │
                                         ▼
                                 ┌──────────────┐
                                 │ FailureHandler│
                                 │ (Async)      │
                                 └──────────────┘
```

## 🛠️ 기술 스택

- **Java 21**
- **Spring Boot 3.2.0**
- **MyBatis 3.0.3** (데이터 영속성)
- **H2 Database** (인메모리)
- **Gradle** (빌드 도구)
- **Lombok & Slf4j** (코드 생성 및 로깅)
- **Strategy Pattern** (재시도 전략 구현)
- **Linear Backoff** (선형 증가 재시도)

## 🚀 빠른 시작

### 사전 요구사항
- Java 21 이상
- Gradle 7.0 이상

### 애플리케이션 실행

```bash
# 프로젝트로 이동
cd rollback

# 애플리케이션 실행
./gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 시작됩니다

### 접근 포인트
- **메인 애플리케이션**: http://localhost:8080
- **API 엔드포인트**: http://localhost:8080/api/orders
- **H2 콘솔**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 사용자명: `sa`
  - 비밀번호: `password`

## 📡 API 엔드포인트

### 주문 관리 API

#### 주문 생성
```http
POST /api/orders
Content-Type: application/json

{
  "customerName": "홍길동",
  "productName": "노트북",
  "quantity": 2,
  "amount": 15000,
  "forcePaymentFailure": false
}
```

#### 주문 수정
```http
PUT /api/orders/{id}
Content-Type: application/json

{
  "customerName": "홍길동",
  "productName": "스마트폰",
  "quantity": 1,
  "amount": 80000
}
```

#### 주문 취소
```http
POST /api/orders/{id}/cancel
```

#### 주문 목록 조회
```http
GET /api/orders
```

#### 특정 주문 조회
```http
GET /api/orders/{id}
```

### 재고 관리 API

#### 전체 재고 조회
```http
GET /api/orders/inventory
```

#### 재고 부족 목록 조회
```http
GET /api/orders/inventory/low-stock
```

#### 신규 재고 등록
```http
POST /api/orders/inventory
Content-Type: application/json

{
  "productName": "태블릿",
  "currentStock": 100,
  "minStockLevel": 10
}
```

### 배송 관리 API

#### 주문별 배송 조회
```http
GET /api/orders/{id}/shipment
```

#### 배송 생성
```http
POST /api/orders/{id}/shipment
Content-Type: application/json

{
  "shippingAddress": "서울시 강남구 테헤란로 123"
}
```

#### 배송 시작
```http
POST /api/orders/shipment/{shipmentId}/ship
Content-Type: application/json

{
  "carrier": "CJ대한통운"
}
```

#### 배송 완료
```http
POST /api/orders/shipment/{shipmentId}/deliver
```

### 응답 예시

**성공 응답**:
```json
{
  "success": true,
  "guid": "abc-123-def",
  "message": "주문이 성공적으로 생성되었습니다",
  "order": {
    "id": 1,
    "customerName": "홍길동",
    "productName": "노트북",
    "quantity": 2,
    "amount": 15000,
    "status": "PAID"
  }
}
```

**실패 응답**:
```json
{
  "success": false,
  "message": "주문 실패: 재고가 부족합니다: 노트북"
}
```

## 🔧 핵심 컴포넌트

### 1. OrderService (`@Transactional`)
```java
@Transactional
public Order create(OrderRequest req) {
    Order order = req.toOrder();
    orders.save(order);  // 저장되지만 아직 커밋되지 않음
    
    try {
        paymentClient.pay(order.getId(), req.getAmount(), req.isForcePaymentFailure());
        orders.updateStatus(order.getId(), "PAID");
        return order;
    } catch (Exception e) {
        // 롤백 후 처리를 위한 이벤트 발행
        events.publishEvent(new OrderFailed(order.getId(), e.getMessage()));
        throw e;  // 핵심: 예외를 다시 던져서 롤백 트리거
    }
}
```

### 2. PaymentClient (Retry Pattern)
```java
@Component
public class PaymentClient {
    private final LockRetryTemplate retryTemplate;
    
    public void pay(String guid, Long orderId, Integer amount, boolean forceFailure) {
        retryTemplate.execute(() -> {
            if (forceFailure) {
                throw new PaymentException("결제 게이트웨이 오류: 연결 시간 초과");
            }
            // 결제 처리 로직
            return null;
        });
    }
}
```

### 3. LockRetryTemplate (Strategy Pattern)
```java
@Component
public class LockRetryTemplate {
    private final RetryStrategy retryStrategy;
    
    public <T> T execute(Supplier<T> action) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return action.get();
            } catch (Exception e) {
                if (retryStrategy.shouldRetry(e, attempt)) {
                    long waitTime = retryStrategy.getWaitTime(attempt);
                    Thread.sleep(waitTime);
                    continue;
                } else {
                    throw e;
                }
            }
        }
    }
}
```

### 4. LinearBackoffRetryStrategy
```java
@Component
public class LinearBackoffRetryStrategy implements RetryStrategy {
    private final int maxAttempts = 5;
    private final long initialDelay = 1000;
    private final long increment = 500;
    
    @Override
    public boolean shouldRetry(Exception e, int attemptCount) {
        return attemptCount < maxAttempts && e instanceof PaymentException;
    }
    
    @Override
    public long getWaitTime(int attemptCount) {
        return initialDelay + (increment * (attemptCount - 1));
    }
}
```

### 5. FailureHandler (`@TransactionalEventListener`)
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
@Async
public void handle(OrderFailed event) {
    notifier.sendFailure(event.getOrderId(), event.getReason());
}
```

### 6. NotificationService (`REQUIRES_NEW`)
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void sendFailure(Long orderId, String reason) {
    // 완전히 별도의 트랜잭션에서 실행
    notificationLogRepository.save(new NotificationLog(orderId, message, "FAILURE"));
}
```

## 📊 데이터베이스 스키마

### 주문 테이블 (orders)
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| guid | VARCHAR(36) | 주문 추적 ID |
| customer_name | VARCHAR(100) | 고객 이름 |
| product_name | VARCHAR(200) | 상품명 |
| amount | INTEGER | 주문 금액 |
| quantity | INTEGER | 주문 수량 |
| status | VARCHAR(20) | 주문 상태 |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |

### 재고 테이블 (inventory)
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| product_name | VARCHAR(200) | 상품명 (UNIQUE) |
| current_stock | INTEGER | 현재 재고 |
| reserved_stock | INTEGER | 예약된 재고 |
| min_stock_level | INTEGER | 최소 재고 레벨 |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |

### 배송 테이블 (shipments)
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| order_id | BIGINT | 관련 주문 ID (FK) |
| tracking_number | VARCHAR(100) | 운송장번호 (UNIQUE) |
| carrier | VARCHAR(50) | 운송사 |
| status | VARCHAR(20) | 배송 상태 |
| shipping_address | VARCHAR(500) | 배송지 주소 |
| estimated_delivery | DATE | 예상 배송일 |
| shipped_at | TIMESTAMP | 배송 시작 시간 |
| delivered_at | TIMESTAMP | 배송 완료 시간 |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |

### 주문 상품 테이블 (order_items)
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| order_id | BIGINT | 관련 주문 ID (FK) |
| product_name | VARCHAR(200) | 상품명 |
| quantity | INTEGER | 수량 |
| unit_price | INTEGER | 단가 |
| total_price | INTEGER | 총 가격 |
| created_at | TIMESTAMP | 생성 시간 |

### 알림 로그 테이블 (notification_logs)
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| guid | VARCHAR(36) | 주문 추적 ID |
| order_id | BIGINT | 관련 주문 ID |
| message | VARCHAR(255) | 알림 메시지 |
| type | VARCHAR(50) | 알림 타입 |
| created_at | TIMESTAMP | 생성 타임스탬프 |

## 🔄 트랜잭션 흐름 분석

### 성공 시나리오
1. `OrderService.create()` 트랜잭션 시작
2. 주문을 `orders` 테이블에 삽입
3. 결제 API 호출 성공
4. 주문 상태를 "PAID"로 업데이트
5. 트랜잭션 **커밋됨**
6. 성공 알림 전송 (별도 트랜잭션)

### 실패 시나리오 (재시도 포함)
1. `OrderService.create()` 트랜잭션 시작
2. 주문을 `orders` 테이블에 삽입 (커밋되지 않음)
3. `PaymentClient.pay()` 호출 → `LockRetryTemplate.execute()` 실행
4. **재시도 1**: 결제 실패 → 1000ms 대기 후 재시도
5. **재시도 2**: 결제 실패 → 1500ms 대기 후 재시도
6. **재시도 3**: 결제 실패 → 2000ms 대기 후 재시도
7. **재시도 4**: 결제 실패 → 2500ms 대기 후 재시도
8. **재시도 5**: 결제 최종 실패 → 예외 발생
9. `OrderFailed` 이벤트 발행
10. 예외 재전달 → 트랜잭션이 **롤백**으로 마크됨
11. 트랜잭션 **롤백됨** (주문이 데이터베이스에서 제거됨)
12. `FailureHandler.handle()` **롤백 후** 실행
13. **새 트랜잭션**에서 실패 알림 전송

## 🎮 애플리케이션 테스트

### 웹 인터페이스
http://localhost:8080에 접속하여 내장된 웹 인터페이스 사용:
- 고객 이름과 금액으로 주문 생성
- "결제 실패 강제 발생" 토글로 롤백 시나리오 테스트
- 실시간 주문 목록 및 실행 로그 확인

### 테스트 시나리오

1. **정상 흐름**:
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customerName":"김철수","amount":25000,"forcePaymentFailure":false}'
   ```

2. **결제 실패 (재시도 및 롤백 테스트)**:
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customerName":"이영희","productName":"노트북","quantity":1,"amount":30000,"forcePaymentFailure":true}'
   ```

3. **결과 확인**:
   ```bash
   # 주문 확인 (실패한 주문은 롤백으로 나타나지 않음)
   curl http://localhost:8080/api/orders
   
   # 알림 로그 확인 (성공, 실패 모두 나타남)
   # H2 콘솔에서 notification_logs 테이블 쿼리
   ```

## 🔍 핵심 학습 포인트

### 트랜잭션 경계
- `@Transactional`이 트랜잭션 경계 생성
- 롤백을 트리거하려면 예외가 전파되어야 함
- 롤백 제어를 위해 `catch` 블록에서 `throw` 필수

### 재시도 패턴
- **Strategy Pattern**: `RetryStrategy` 인터페이스로 다양한 재시도 전략 구현 가능
- **Linear Backoff**: 대기 시간을 1000ms → 1500ms → 2000ms → 2500ms → 3000ms으로 점진적 증가
- **예외 필터링**: `PaymentException`만 재시도 대상, 다른 예외는 즉시 실패 처리
- **최대 재시도**: 5회로 설정하여 무한 재시도 방지

### 이벤트 기반 롤백 처리
- `@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)`이 롤백 후 실행 보장
- 이벤트는 롤백 전에 발행되지만 롤백 후에 실행
- `@Async`로 메인 스레드 블로킹 방지

### 트랜잭션 전파
- `REQUIRES_NEW`가 독립적 트랜잭션 생성
- 로깅, 알림, 감사 추적에 유용
- 메인 트랜잭션 결과와 상관없이 데이터 영속성 보장

## 🚨 일반적인 실수

1. **`throw e` 누락**: 예외를 다시 던지지 않으면 Spring이 롤백하지 않음
2. **재시도 로직 오류**: 무한 재시도로 서버 자원 고갈 발생
3. **잘못된 예외 필터링**: 모든 예외를 재시도 대상으로 설정하여 비즈니스 오류 반복
4. **잘못된 이벤트 단계**: `AFTER_ROLLBACK` 대신 `AFTER_COMMIT` 사용
5. **트랜잭션 전파**: 알림 영속성을 위한 `REQUIRES_NEW` 누락
6. **비동기 설정**: 메인 애플리케이션 클래스에 `@EnableAsync` 누락

## 📁 프로젝트 구조

```
src/main/java/com/example/rollback/
├── RollbackApplication.java          # 메인 애플리케이션 클래스
├── config/
│   ├── AsyncConfig.java             # 비동기 처리 설정
│   ├── RetryConfig.java            # 재시도 빈 설정
│   └── ContextFilter.java          # GUID 컨텍스트 필터
├── controller/
│   └── OrderController.java          # REST API 엔드포인트
├── domain/
│   ├── Order.java                   # 주문 엔티티
│   ├── OrderRequest.java            # 주문 생성 DTO
│   ├── Inventory.java               # 재고 엔티티
│   ├── Shipment.java               # 배송 엔티티
│   ├── NotificationLog.java         # 알림 로그 엔티티
│   └── *(Request.java)             # 각종 DTO들
├── event/
│   ├── OrderFailed.java             # 실패 이벤트
│   └── FailureHandler.java          # 이벤트 리스너
├── exception/
│   ├── OrderException.java          # 주문 관련 예외
│   └── PaymentException.java        # 결제 관련 예외
├── repository/
│   ├── OrderRepository.java         # 주문 MyBatis 매퍼
│   ├── InventoryRepository.java     # 재고 MyBatis 매퍼
│   ├── ShipmentRepository.java      # 배송 MyBatis 매퍼
│   └── NotificationLogRepository.java # 알림 로그 매퍼
├── retry/
│   ├── RetryStrategy.java           # 재시도 전략 인터페이스
│   ├── LinearBackoffRetryStrategy.java # 선형 증가 재시도 전략
│   ├── LockRetryTemplate.java       # 재시도 템플릿
│   └── RetryableException.java     # 재시도 가능 예외
├── service/
│   ├── OrderService.java            # 메인 비즈니스 로직
│   ├── PaymentClient.java           # 외부 결제 시뮬레이션
│   ├── InventoryService.java       # 재고 관리
│   ├── ShipmentService.java        # 배송 관리
│   └── NotificationService.java     # 알림 처리
├── util/
│   ├── ContextHolder.java          # GUID 컨텍스트 관리
│   ├── GuidQueue.java             # GUID 큐
│   ├── GuidQueueUtil.java         # GUID 유틸리티
│   └── CtxMap.java               # 컨텍스트 맵
└── resources/
    ├── application.yml              # 애플리케이션 설정
    ├── schema.sql                   # 데이터베이스 스키마
    └── static/
        ├── index.html               # 웹 인터페이스
        ├── script.js                # 프론트엔드 로직
        └── style.css                # 프론트엔드 스타일
```

## 🤝 기여

학습용 예제 프로젝트입니다. 자유롭게 다음을 수행할 수 있습니다:
- 다양한 트랜잭션 시나리오 실험
- 더 복잡한 비즈니스 로직 추가
- 다른 데이터베이스로 테스트
- 추가적인 실패 처리 패턴 구현

## 📄 라이선스

이 프로젝트는 교육 목적으로 제공됩니다. 학습을 위해 자유롭게 사용하고 수정할 수 있습니다.