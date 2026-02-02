# Spring Boot 뱅킹 시스템 (트랜잭션 롤백)

스프링 부트(Spring Boot)로 구축된 포괄적인 뱅킹 시스템 데모로, 트랜잭션 롤백, 이벤트 기반 아키텍처, 최신 엔터프라이즈 자바 패턴을 시연합니다.

## 개요

이 프로젝트는 다음을 시연하는 **뱅킹 시스템 시뮬레이션**입니다:
- 트랜잭션 관리 및 롤백 처리
- 실패 알림을 위한 이벤트 기반 아키텍처
- 전략 패턴을 적용한 설정 가능한 재시도 메커니즘
- `ThreadLocal` 및 GUID를 사용한 요청 컨텍스트 추적
- 적절한 컨텍스트 전파를 통한 비동기 처리
- 관심사의 명확한 분리 (MVC 패턴)

## 기술 스택

| 기술 | 버전 | 목적 |
|------------|---------|---------|
| **Spring Boot** | 3.2.0 | 핵심 프레임워크 |
| **Java** | 21 | 프로그래밍 언어 |
| **Spring Web** | 3.2.0 | REST API 개발 |
| **Spring JDBC** | 3.2.0 | 데이터베이스 접근 |
| **MyBatis** | 3.0.3 | ORM 프레임워크 |
| **H2 Database** | 최신 | 개발용 인메모리 데이터베이스 |
| **Lombok** | 최신 | 보일러플레이트 코드 감소 |
| **Validation** | 3.2.0 | 빈(Bean) 유효성 검사 |

## 주요 기능

### 1. 뱅킹 오퍼레이션

- **계좌 관리**
  - 입출금, 저축, 신용, 비즈니스 계좌 생성
  - 계좌 상태 관리 (활성, 동결, 해지)
  - 통화 지원 (KRW, USD, EUR)
  - 마이너스 한도 설정

- **트랜잭션 처리**
  - 외부 결제 게이트웨이 시뮬레이션을 통한 입금 처리
  - 계좌 간 이체
  - 거래 내역 추적
  - 참조 번호 생성

- **고객 관리**
  - 개인 및 비즈니스 고객 유형
  - 위험 등급 평가 (낮음, 중간, 높음)
  - 고객 상태 추적

### 2. 트랜잭션 롤백 및 이벤트 처리

- **`@Transactional`**을 통한 ACID 준수
- 실패 시 **`TransactionFailed`** 이벤트 발행
- 롤백 후 **`@TransactionalEventListener`** 트리거
- 컨텍스트를 보존하는 **비동기 알림** 처리

### 3. 재시도 메커니즘 (Strategy & Composite Pattern)

- **전략 패턴(Strategy Pattern) 기반의 유연한 재시도 로직**: `LockRetryTemplate`을 사용하여 재시도 로직을 비즈니스 코드와 분리.
- **`RandomBackoffRetryStrategy`**: 랜덤 지터(Jitter)를 포함한 백오프 전략을 사용하여 여러 인스턴스에서 동시에 재시도를 시작하여 발생하는 "Thundering Herd" 문제를 방지합니다.
    - 최대 재시도 횟수: 10회
    - 기본 대기 시간: 100ms
- **조건 기반 재시도 결정 (Composite Pattern)**: `RetryCondition` 인터페이스를 통해 재시도 조건을 동적으로 추가 및 조합할 수 있습니다.
    - `LockRetryCondition`: 비관적 락(Pessimistic Lock) 실패 또는 관련 DB 락 타임아웃 시 재시도합니다.
    - `DeadlockRetryCondition`: 데이터베이스 데드락 발생 시 재시도합니다.
- 외부 결제 게이트웨이 연동과 같은 실패 가능성이 있는 작업에 적용됩니다.

### 4. 요청 컨텍스트 추적

- **GUID 기반** 요청 상관관계 추적
- 요청별 **`ThreadLocal`** 컨텍스트 저장
- 구조화된 로깅을 위한 **MDC (Mapped Diagnostic Context)**
- 클라이언트 정보 추적:
  - IP 주소
  - User-Agent
  - 세션 ID

### 5. 비동기 처리

- **`@EnableAsync`**와 커스텀 스레드 풀
- 스레드 풀: 2-5개 스레드
- 오류 처리를 위한 **`AsyncUncaughtExceptionHandler`**
- 비동기 스레드로 컨텍스트 전파

## 프로젝트 구조

```
src/main/java/com/example/rollback/
├── RollbackApplication.java          # 메인 애플리케이션
├── controller/
│   ├── BankingController.java        # 뱅킹 REST API
│   └── CustomerController.java       # 고객 REST API
├── service/
│   ├── AccountService.java           # 계좌 비즈니스 로직
│   ├── CustomerService.java          # 고객 비즈니스 로직
│   ├── PaymentClient.java            # 결제 게이트웨이 시뮬레이션
│   └── ...                           # 기타 서비스
├── repository/
│   ├── AccountRepository.java        # 데이터 접근 계층
│   ├── CustomerRepository.java
│   └── ...
├── domain/
│   ├── Account.java                  # 계좌 엔티티
│   ├── Customer.java                 # 고객 엔티티
│   ├── Transaction.java              # 거래 엔티티
│   └── ...                           # DTO 및 Enum
├── event/
│   ├── TransactionFailed.java        # 실패 이벤트
│   └── TransactionFailureHandler.java # 롤백 핸들러
├── retry/
│   ├── LockRetryTemplate.java        # 재시도 템플릿
│   └── strategy/                     # 전략 구현 패키지
│       ├── RandomBackoffRetryStrategy.java # 랜덤 백오프 전략
│       ├── RetryCondition.java       # 재시도 조건 인터페이스
│       ├── LockRetryCondition.java   # 락 충돌 재시도 조건
│       └── DeadlockRetryCondition.java # 데드락 재시도 조건
├── util/
│   ├── ContextHolder.java            # ThreadLocal 컨텍스트
│   └── GuidQueueUtil.java            # GUID 생성
├── config/
│   ├── AsyncConfig.java              # 비동기 설정
│   ├── RetryConfig.java              # 재시도 설정
│   └── ...
└── exception/
    └── PaymentException.java         # 결제 예외
```

## API 엔드포인트

### 뱅킹 API (`/api/banking`)

| 엔드포인트 | 메서드 | 설명 |
|----------|--------|-------------|
| `/api/banking/accounts` | POST | 새 은행 계좌 생성 |
| `/api/banking/accounts` | GET | 모든 계좌 목록 조회 |
| `/api/banking/accounts/{id}` | GET | ID로 계좌 조회 |
| `/api/banking/accounts/customer/{customerId}` | GET | 고객의 계좌 목록 조회 |
| `/api/banking/deposit` | POST | 입금 |
| `/api/banking/transfer` | POST | 계좌 이체 |
| `/api/banking/transactions` | GET | 모든 거래 내역 조회 |

### 고객 API (`/api/banking/customers`)

| 엔드포인트 | 메서드 | 설명 |
|----------|--------|-------------|
| `/api/banking/customers` | POST | 새 고객 생성 |
| `/api/banking/customers` | GET | 모든 고객 목록 조회 |
| `/api/banking/customers/{id}` | GET | ID로 고객 조회 |
| `/api/banking/customers/{id}` | PUT | 고객 정보 업데이트 |

### 추가 엔드포인트

| 엔드포인트 | 설명 |
|----------|-------------|
| `/h2-console` | H2 데이터베이스 콘솔 (개발용) |
| `/` | 정적 웹 UI (index.html) |

## 요청 예시

### 계좌 생성
```bash
curl -X POST http://localhost:8080/api/banking/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "accountType": "CHECKING",
    "currency": "KRW",
    "initialDeposit": 100000,
    "forceFailure": false
  }'
```

### 입금
```bash
curl -X POST http://localhost:8080/api/banking/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "customerId": 1,
    "amount": 50000,
    "currency": "KRW",
    "description": "월급 입금",
    "forceFailure": false
  }'
```

### 이체
```bash
curl -X POST http://localhost:8080/api/banking/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "customerId": 1,
    "amount": 10000,
    "currency": "KRW",
    "description": "월세 이체",
    "forceFailure": false
  }'
```

## 설정

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.example.rollback.domain

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 주요 디자인 패턴

### 1. 트랜잭션 관리

```java
@Transactional
public Account createAccount(AccountRequest request) {
    // 비즈니스 로직
    transactionRepository.save(transaction);
    
    if (request.isForceFailure()) {
        throw new PaymentException("시뮬레이션된 실패");
    }
    
    // 예외 발생 시 트랜잭션은 자동으로 롤백됩니다.
}
```

### 2. 이벤트 기반 롤백 처리

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
@Async
public void handleTransactionFailed(TransactionFailed event) {
    // 이 코드는 트랜잭션 롤백이 완료된 후에만 실행됩니다.
    notificationService.sendFailureNotification(event);
}
```

### 3. 전략 패턴 기반의 재시도

```java
// 재시도 템플릿을 사용하여 결제 처리 실행
retryTemplate.executeWithRetry(() -> {
    return paymentGateway.processPayment(request);
});

// RetryConfig에서 전략과 조건을 주입하여 유연성 확보
@Bean
public RetryStrategy retryStrategy(List<RetryCondition> conditions) {
    return new RandomBackoffRetryStrategy(10, 100, 200, 2000, conditions);
}
```

### 4. 컨텍스트 추적

```java
// 요청 시작 시 컨텍스트 초기화
ContextHolder.initializeContext(guid);
MDC.put("guid", guid);

// 동일 스레드 내 어디서든 접근 가능
String guid = ContextHolder.getContext().getGuid();
```

## 시작하기

### 사전 요구사항

- Java 21 이상
- Gradle 8.x 이상

### 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 파일 직접 실행
java -jar build/libs/*.jar
```

### 애플리케이션 접속

- **웹 UI**: http://localhost:8080
- **API Base**: http://localhost:8080/api/banking
- **H2 콘솔**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 사용자명: `sa`
  - 비밀번호: (없음)

## 롤백 테스트

웹 UI에서 "실패 강제" 체크박스를 활성화하거나 API 요청 시 `"forceFailure": true`를 설정하여 트랜잭션 실패 및 롤백 동작을 시뮬레이션하고 관찰할 수 있습니다.

## 프론트엔드

이 프로젝트는 반응형 웹 인터페이스를 포함합니다:
- **index.html**: 메인 뱅킹 대시보드
- **banking-style.css**: 모던한 파란색 테마 스타일링
- **script.js**: 실시간 로깅을 포함한 인터랙티브 JavaScript

특징:
- 탭 기반 네비게이션 (계좌, 고객, 거래내역)
- 실시간 실행 로그
- 폼 유효성 검사
- 계좌 동결/활성화 기능

## 개발 참고사항

### 트랜잭션 흐름
1. 컨트롤러가 요청 수신
2. 서비스 메서드 시작 (`@Transactional`)
3. 데이터베이스 작업 실행
4. 성공 시: 트랜잭션 커밋
5. 실패 시: 롤백 + `TransactionFailed` 이벤트 발행
6. 이벤트 리스너가 비동기 알림 전송

### 요청 추적
- 각 요청은 고유한 GUID를 가짐
- GUID는 MDC를 통해 로그 전체에 전파됨
- 요청 처리 후 컨텍스트는 자동으로 정리됨

## 파일 통계

- **총 Java 파일**: 49
- **코드 라인 수**: 약 3,500+ (Java)
- **프론트엔드**: HTML, CSS, JavaScript
- **빌드 도구**: Gradle

## 라이선스

이 프로젝트는 교육 및 데모 목적으로 제작되었습니다.

---

*소스 분석을 통해 종합적으로 생성됨*
