# 🔄 Spring Boot 트랜잭션 롤백 및 이벤트 처리 예제

결제 실패 시나리오에서 트랜잭션 롤백 처리와 롤백 완료 후 이벤트 처리를 보여주는 Spring Boot 애플리케이션입니다.

## 📋 개요

이 프로젝트는 외부 API 호출 실패 시 트랜잭션 롤백을 올바르게 처리하고, 롤백이 완료된 후에만 실패 알림을 실행하는 방법을 보여줍니다. 핵심 패턴은 **"롤백 후 알림"** 입니다:

1. 주문 데이터를 데이터베이스에 저장
2. 외부 결제 API 호출
3. 결제 실패 시 트랜잭션 롤백 (주문 데이터 제거)
4. 롤백 성공 후에만 별도 트랜잭션으로 실패 알림 전송

## 🎯 학습 목표

- **트랜잭션 관리**: Spring의 `@Transactional` 경계 이해
- **이벤트 기반 아키텍처**: 롤백 후 처리를 위한 `@TransactionalEventListener` 사용
- **트랜잭션 전파**: 독립적 트랜잭션을 위한 `REQUIRES_NEW`
- **에러 처리**: 롤백을 트리거하는 올바른 예외 처리
- **비동기 처리**: 블로킹 없는 실패 알림

## 🏗️ 아키텍처

```
┌─────────────┐    ┌──────────────┐    ┌─────────────────┐
│ Controller  │───▶│ OrderService │───▶│ PaymentClient   │
│             │    │              │    │                 │
└─────────────┘    └──────┬───────┘    └─────────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ Repository   │
                    │ (Orders)     │
                    └──────────────┘
                           │
                           ▼ (실패 시)
                    ┌──────────────┐
                    │ Event        │
                    │ Publisher    │
                    └──────┬───────┘
                           │
                           ▼ (롤백 후)
                    ┌──────────────┐    ┌─────────────────┐
                    │ FailureHandler│───▶│ NotificationSvc │
                    │ (Async)      │    │ (New Transaction)│
                    └──────────────┘    └─────────────────┘
```

## 🛠️ 기술 스택

- **Java 21**
- **Spring Boot 3.2.0**
- **MyBatis 3.0.3** (데이터 영속성)
- **H2 Database** (인메모리)
- **Gradle** (빌드 도구)
- **Lombok & Slf4j** (코드 생성 및 로깅)

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

### 주문 생성
```http
POST /api/orders
Content-Type: application/json

{
  "customerName": "홍길동",
  "amount": 15000,
  "forcePaymentFailure": false
}
```

### 응답 예시

**성공 응답**:
```json
{
  "success": true,
  "message": "주문이 성공적으로 생성되었습니다",
  "order": {
    "id": 1,
    "customerName": "홍길동",
    "amount": 15000,
    "status": "PAID"
  }
}
```

**실패 응답**:
```json
{
  "success": false,
  "message": "주문 실패: Payment failed for order 1"
}
```

### 기타 엔드포인트
- `GET /api/orders` - 모든 주문 목록
- `GET /api/orders/{id}` - 특정 주문 조회

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

### 2. FailureHandler (`@TransactionalEventListener`)
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
@Async
public void handle(OrderFailed event) {
    notifier.sendFailure(event.getOrderId(), event.getReason());
}
```

### 3. NotificationService (`REQUIRES_NEW`)
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void sendFailure(Long orderId, String reason) {
    // 완전히 별도의 트랜잭션에서 실행
    notificationLogRepository.save(new NotificationLog(orderId, message, "FAILURE"));
}
```

## 📊 데이터베이스 스키마

### 주문 테이블
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| customer_name | VARCHAR(100) | 고객 이름 |
| amount | INTEGER | 주문 금액 |
| status | VARCHAR(20) | 주문 상태 (CREATED, PAID) |

### 알림 로그 테이블
| 컬럼 | 타입 | 설명 |
|--------|------|-------------|
| id | BIGINT | 기본 키 (자동 증가) |
| order_id | BIGINT | 관련 주문 ID |
| message | VARCHAR(255) | 알림 메시지 |
| type | VARCHAR(50) | 알림 타입 (SUCCESS, FAILURE) |
| created_at | TIMESTAMP | 생성 타임스탬프 |

## 🔄 트랜잭션 흐름 분석

### 성공 시나리오
1. `OrderService.create()` 트랜잭션 시작
2. 주문을 `orders` 테이블에 삽입
3. 결제 API 호출 성공
4. 주문 상태를 "PAID"로 업데이트
5. 트랜잭션 **커밋됨**
6. 성공 알림 전송 (별도 트랜잭션)

### 실패 시나리오
1. `OrderService.create()` 트랜잭션 시작
2. 주문을 `orders` 테이블에 삽입 (커밋되지 않음)
3. 결제 API 호출 실패 → 예외 발생
4. `OrderFailed` 이벤트 발행
5. 예외 재전달 → 트랜잭션이 **롤백**으로 마크됨
6. 트랜잭션 **롤백됨** (주문이 데이터베이스에서 제거됨)
7. `FailureHandler.handle()` **롤백 후** 실행
8. **새 트랜잭션**에서 실패 알림 전송

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

2. **결제 실패 (롤백 테스트)**:
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customerName":"이영희","amount":30000,"forcePaymentFailure":true}'
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
2. **잘못된 이벤트 단계**: `AFTER_ROLLBACK` 대신 `AFTER_COMMIT` 사용
3. **트랜잭션 전파**: 알림 영속성을 위한 `REQUIRES_NEW` 누락
4. **비동기 설정**: 메인 애플리케이션 클래스에 `@EnableAsync` 누락

## 📁 프로젝트 구조

```
src/main/java/com/example/rollback/
├── RollbackApplication.java          # 메인 애플리케이션 클래스
├── controller/
│   └── OrderController.java          # REST API 엔드포인트
├── domain/
│   ├── Order.java                   # 주문 엔티티
│   ├── OrderRequest.java            # 주문 생성 DTO
│   └── NotificationLog.java         # 알림 로그 엔티티
├── event/
│   ├── OrderFailed.java             # 실패 이벤트
│   └── FailureHandler.java          # 이벤트 리스너
├── repository/
│   ├── OrderRepository.java         # 주문 MyBatis 매퍼
│   └── NotificationLogRepository.java # 알림 로그 매퍼
├── service/
│   ├── OrderService.java            # 메인 비즈니스 로직
│   ├── PaymentClient.java           # 외부 결제 시뮬레이션
│   └── NotificationService.java     # 알림 처리
└── resources/
    ├── application.yml              # 애플리케이션 설정
    ├── schema.sql                   # 데이터베이스 스키마
    ├── mapper/
    │   ├── OrderMapper.xml          # 주문 SQL 매핑
    │   └── NotificationLogMapper.xml # 알림 로그 SQL 매핑
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