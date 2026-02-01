# 🏦 은행 계좌 관리 시스템

<div align="center">

**Spring Boot 기반 실무용 은행 계좌 관리 시스템**

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![MyBatis](https://img.shields.io/badge/MyBatis-3.0.3-red)
![License](https://img.shields.io/badge/License-Educational-blue)

</div>

## 📋 개요

이 프로젝트는 **실무용 은행 계좌 관리 시스템**으로, 계좌 개설, 입출금, 이체, 고객 관리, 거래 내역 추적 등 금융 서비스의 핵심 기능들을 완벽하게 구현합니다.

Spring Boot 3.2.0, Java 21, MyBatis 기반으로 구축되었으며, 트랜잭션 롤백, 재시도 메커니즘, 감사 로그, 비동기 이벤트 처리 등 엔터프라이즈 수준의 기능들을 포함합니다.

## ✨ 핵심 기능

### 🏦 은행 서비스
- **계좌 개설**: 입출금, 적금, 신용, 사업자 계좌 생성
- **입출금 처리**: 실시간 입금/출금 기능
- **계좌 이체**: 계좌 간 자금 이체 (원자성 보장)
- **거래 내역**: 실시간 거래 추적 및 상세 조회
- **계좌 관리**: 계좌 동결, 활성화, 상태 변경

### 👥 고객 관리
- **고객 등록**: 개인/법인 고객 등록
- **고객 조회**: 고객별 계좌 목록 조회
- **리스크 관리**: LOW/MEDIUM/HIGH 리스크 레벨 분류
- **고객 상태**: ACTIVE/INACTIVE/SUSPENDED/CLOSED 관리

### 🔒 보안 및 감사
- **감사 로그**: 계좌/거래 모든 작업에 대한 완전한 감사 추적
- **로그인 기록**: 모든 로그인 시도와 실패 기록
- **IP 추적**: 클라이언트 IP, User-Agent, Session ID 로깅
- **거래 추적**: GUID 기반 거래 추적

### 🔄 트랜잭션 관리
- **선언적 트랜잭션**: @Transactional 어노테이션 기반 관리
- **자동 롤백**: 결제 실패 시 트랜잭션 자동 롤백
- **롤백 후 처리**: @TransactionalEventListener로 롤백 후 알림 발송
- **독립적 트랜잭션**: 알림/로그를 위한 REQUIRES_NEW 전파

### ⚡ 성능 최적화
- **전략적 인덱스**: 20개 이상의 성능 최적화 인덱스
- **캐싱**: GUID 큐 미리 생성으로 성능 향상
- **연결 풀**: HikariCP로 DB 연결 최적화
- **MyBatis XML**: 복잡한 쿼리 XML에서 관리

## 🛠️ 기술 스택

### 백엔드
| 기술 | 버전 | 설명 |
|------|--------|------|
| **Java** | 21 | 최신 LTS 버전 |
| **Spring Boot** | 3.2.0 | Spring Boot 3.x |
| **MyBatis** | 3.0.3 | ORM Framework |
| **H2 Database** | 2.x | In-memory DB (개발용) |
| **Gradle** | 8.5 | Build Tool |
| **Lombok** | Latest | 코드 자동 생성 |
| **SLF4J** | 2.x | Logging Framework |

### 핵심 라이브러리
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    implementation 'com.h2database:h2'
    implementation 'org.projectlombok:lombok'
    implementation 'org.slf4j:slf4j-api'
}
```

### 프론트엔드
- **HTML5 + CSS3**: 시맨틱 웹
- **Vanilla JavaScript (ES6+)**: 모던 자바스크립트
- **Font Awesome 6.4.0**: 아이콘 라이브러리

## 🚀 빠른 시작

### 사전 요구사항

- **Java 21** 이상 설치
- **Gradle 7.0** 이상
- **Git** (선택 사항)

### 설치 및 실행

```bash
# 1. 프로젝트 클론
git clone <repository-url>
cd rollback

# 2. 의존성 다운로드 및 빌드
./gradlew build

# 3. 애플리케이션 실행
./gradlew bootRun
```

### 접속 정보

| 서비스 | URL | 설명 |
|--------|-----|------|
| **메인 애플리케이션** | http://localhost:8080 | 웹 인터페이스 |
| **H2 콘솔** | http://localhost:8080/h2-console | 데이터베이스 관리 |
| **Base API** | http://localhost:8080/api/banking | REST API 베이스 |

**H2 콘솔 접속 정보:**
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **사용자명**: `sa`
- **비밀번호**: (비어있음)

## 📊 데이터베이스 스키마

### 핵심 테이블 구조

#### 1. customers (고객 테이블)

| 컬럼 | 타입 | 설명 | 제약조건 |
|--------|------|-------------|-----------|
| id | BIGINT | 기본 키 | PK, AUTO_INCREMENT |
| customer_number | VARCHAR(20) | 고객 번호 | UNIQUE, NOT NULL |
| name | VARCHAR(100) | 성명 | NOT NULL |
| email | VARCHAR(150) | 이메일 | NOT NULL, CHECK (LIKE '%@%.%') |
| phone_number | VARCHAR(20) | 연락처 | NOT NULL |
| customer_type | VARCHAR(20) | 고객 유형 | CHECK (INDIVIDUAL, BUSINESS) |
| risk_level | VARCHAR(20) | 리스크 레벨 | CHECK (LOW, MEDIUM, HIGH) |
| status | VARCHAR(20) | 상태 | CHECK (ACTIVE, INACTIVE, SUSPENDED, CLOSED) |
| created_at | TIMESTAMP | 생성 시간 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | 수정 시간 | ON UPDATE CURRENT_TIMESTAMP |

#### 2. accounts (계좌 테이블)

| 컬럼 | 타입 | 설명 | 제약조건 |
|--------|------|-------------|-----------|
| id | BIGINT | 기본 키 | PK, AUTO_INCREMENT |
| account_number | VARCHAR(30) | 계좌번호 | UNIQUE, NOT NULL |
| customer_id | BIGINT | 고객 ID | FK customers(id) |
| account_type | VARCHAR(20) | 계좌 유형 | CHECK (CHECKING, SAVINGS, CREDIT, BUSINESS) |
| currency | VARCHAR(10) | 통화 | DEFAULT 'KRW' |
| balance | DECIMAL(19,2) | 잔액 | NOT NULL, DEFAULT 0.00, CHECK (>= 0) |
| overdraft_limit | DECIMAL(19,2) | 초과한도 | NOT NULL, DEFAULT 0.00, CHECK (>= 0) |
| account_holder_name | VARCHAR(100) | 예금주명 | NOT NULL |
| branch_code | VARCHAR(10) | 지점 코드 | - |
| status | VARCHAR(20) | 계좌 상태 | CHECK (ACTIVE, FROZEN, CLOSED, SUSPENDED) |
| daily_transaction_limit | DECIMAL(19,2) | 일일 거래 한도 | DEFAULT 1000000.00 |
| monthly_transaction_limit | DECIMAL(19,2) | 월간 거래 한도 | DEFAULT 5000000.00 |
| daily_transaction_amount | DECIMAL(19,2) | 일일 거래 금액 | DEFAULT 0.00 |
| monthly_transaction_amount | DECIMAL(19,2) | 월간 거래 금액 | DEFAULT 0.00 |
| created_at | TIMESTAMP | 생성 시간 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | 수정 시간 | ON UPDATE CURRENT_TIMESTAMP |

#### 3. transactions (거래 테이블)

| 컬럼 | 타입 | 설명 | 제약조건 |
|--------|------|-------------|-----------|
| id | BIGINT | 기본 키 | PK, AUTO_INCREMENT |
| guid | VARCHAR(36) | 거래 추적 ID | UNIQUE |
| from_account_id | BIGINT | 출금 계좌 | FK accounts(id) |
| to_account_id | BIGINT | 입금 계좌 | FK accounts(id) |
| customer_id | BIGINT | 고객 ID | FK customers(id) |
| transaction_type | VARCHAR(20) | 거래 유형 | CHECK (DEPOSIT, WITHDRAWAL, TRANSFER, FEE, INTEREST, PENALTY) |
| amount | DECIMAL(19,2) | 거래 금액 | NOT NULL, CHECK (> 0) |
| currency | VARCHAR(10) | 통화 | DEFAULT 'KRW' |
| description | VARCHAR(500) | 적요 | - |
| status | VARCHAR(20) | 거래 상태 | CHECK (PENDING, COMPLETED, FAILED, CANCELLED, REVERSED) |
| reference_number | VARCHAR(50) | 참조번호 | - |
| failure_reason | VARCHAR(500) | 실패 사유 | - |
| ip_address | VARCHAR(45) | 클라이언트 IP | 보안 로깅 |
| device_info | VARCHAR(100) | 디바이스 정보 | 보안 로깅 |
| transaction_channel | VARCHAR(20) | 거래 채널 | CHECK (ONLINE, MOBILE, ATM, BRANCH, API) |
| transaction_category | VARCHAR(30) | 거래 카테고리 | - |
| fee_amount | DECIMAL(19,2) | 수수료 | DEFAULT 0.00, CHECK (>= 0) |
| balance_after | DECIMAL(19,2) | 거래 후 잔액 | - |
| created_by | VARCHAR(50) | 생성자 | - |
| approved_by | VARCHAR(50) | 승인자 | - |
| approved_at | TIMESTAMP | 승인 시간 | - |
| created_at | TIMESTAMP | 생성 시간 | DEFAULT CURRENT_TIMESTAMP |
| completed_at | TIMESTAMP | 완료 시간 | - |

#### 4. 감사 로그 테이블

**account_audit_log** (계좌 감사):
- 계좌 모든 작업 (INSERT, UPDATE, DELETE, FREEZE, ACTIVATE) 기록
- old_values, new_values (JSON)로 변경 전후 데이터 저장

**transaction_audit_log** (거래 감사):
- 거래 라이프사이클 (CREATE, COMPLETE, FAIL, CANCEL, REVERSE, APPROVE, REJECT) 기록
- 상태 변경 이력 추적

**login_logs** (로그인 기록):
- 모든 로그인 시도 (SUCCESS, FAILED, LOCKED, SUSPICIOUS) 기록
- IP 주소, User-Agent, 세션 ID 저장

### 인덱스 전략

#### 성능 최적화 인덱스

```sql
-- 고객 관련 인덱스
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_risk_level ON customers(risk_level);

-- 계좌 관련 인덱스
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);

-- 거래 관련 인덱스
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_customer ON transactions(customer_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_guid ON transactions(guid);
CREATE INDEX idx_transactions_reference ON transactions(reference_number);

-- 감사 로그 인덱스
CREATE INDEX idx_account_audit_account_id ON account_audit_log(account_id);
CREATE INDEX idx_account_audit_changed_at ON account_audit_log(changed_at);
CREATE INDEX idx_transaction_audit_transaction_id ON transaction_audit_log(transaction_id);
CREATE INDEX idx_transaction_audit_account_id ON transaction_audit_log(account_id);
CREATE INDEX idx_login_logs_customer_id ON login_logs(customer_id);
CREATE INDEX idx_login_logs_login_at ON login_logs(login_at);
CREATE INDEX idx_login_logs_status ON login_logs(login_status);
CREATE INDEX idx_login_logs_ip_address ON login_logs(ip_address);
```

## 📡 API 엔드포인트

### Base URL
```
http://localhost:8080/api/banking
```

### 계좌 관리 API

#### 1. 계좌 개설

```http
POST /api/banking/accounts
Content-Type: application/json

{
  "customerId": 1,
  "accountHolderName": "김철수",
  "accountType": "CHECKING",
  "currency": "KRW",
  "initialDeposit": 100000,
  "branchCode": "001",
  "forceFailure": false
}
```

**응답 (성공)**:
```json
{
  "success": true,
  "guid": "35FG8GN5A6D00001",
  "message": "계좌가 성공적으로 개설되었습니다",
  "account": {
    "id": 1,
    "accountNumber": "ACC17067798901234",
    "customerId":1,
    "accountHolderName": "김철수",
    "accountType": "CHECKING",
    "currency": "KRW",
    "balance": 100000.00,
    "status": "ACTIVE",
    "createdAt": "2026-02-01T14:30:00"
  }
}
```

**응답 (실패)**:
```json
{
  "success": false,
  "message": "계좌 개설 실패: 고객을 찾을 수 없습니다."
}
```

#### 2. 전체 계좌 조회

```http
GET /api/banking/accounts
```

**응답**:
```json
[
  {
    "id": 1,
    "accountNumber": "ACC17067798901234",
    "customerName": "김철수",
    "accountType": "CHECKING",
    "balance": 100000.00,
    "status": "ACTIVE",
    "createdAt": "2026-02-01T14:30:00"
  }
]
```

#### 3. 특정 계좌 조회

```http
GET /api/banking/accounts/{id}
```

#### 4. 고객별 계좌 조회

```http
GET /api/banking/accounts/customer/{customerId}
```

### 거래 처리 API

#### 1. 입금

```http
POST /api/banking/deposit
Content-Type: application/json

{
  "accountId": 1,
  "customerId": 1,
  "amount": 50000,
  "currency": "KRW",
  "description": "월급 입금",
  "forceFailure": false
}
```

**응답 (성공)**:
```json
{
  "success": true,
  "guid": "35FG8GN5A6D00002",
  "message": "입금이 성공적으로 처리되었습니다",
  "transaction": {
    "id": 1,
    "guid": "35FG8GN5A6D00002",
    "toAccountId": 1,
    "transactionType": "DEPOSIT",
    "amount": 50000.00,
    "status": "COMPLETED",
    "referenceNumber": "REF123456789"
  }
}
```

#### 2. 이체

```http
POST /api/banking/transfer
Content-Type: application/json

{
  "fromAccountId": 1,
  "toAccountId": 2,
  "customerId": 1,
  "amount": 100000,
  "currency": "KRW",
  "description": "이체",
  "forceFailure": false
}
```

**응답 (성공)**:
```json
{
  "success": true,
  "guid": "35FG8GN5A6D00003",
  "message": "이체가 성공적으로 처리되었습니다",
  "transaction": {
    "id": 2,
    "guid": "35FG8GN5A6D00003",
    "fromAccountId": 1,
    "toAccountId": 2,
    "transactionType": "TRANSFER",
    "amount": 100000.00,
    "status": "COMPLETED"
  }
}
```

### 고객 관리 API

#### 1. 고객 등록

```http
POST /api/banking/customers
Content-Type: application/json

{
  "name": "이영희",
  "email": "leeyounghee@example.com",
  "phoneNumber": "010-2345-6789",
  "customerType": "INDIVIDUAL",
  "address": "서울시 서초구 강남대로 456",
  "riskLevel": "LOW"
}
```

**응답 (성공)**:
```json
{
  "success": true,
  "guid": "35FG8GN5A6D00004",
  "message": "고객이 성공적으로 등록되었습니다",
  "customer": {
    "id": 4,
    "customerNumber": "CUST17067798905678",
    "name": "이영희",
    "email": "leeyounghee@example.com",
    "phoneNumber": "010-2345-6789",
    "customerType": "INDIVIDUAL",
    "riskLevel": "LOW",
    "status": "ACTIVE",
    "createdAt": "2026-02-01T14:31:00"
  }
}
```

#### 2. 전체 고객 조회

```http
GET /api/banking/customers
```

#### 3. 특정 고객 조회

```http
GET /api/banking/customers/{id}
```

## 📁 프로젝트 구조

```
src/main/java/com/example/rollback/
├── RollbackApplication.java              # 메인 애플리케이션
├── config/
│   ├── AsyncConfig.java                 # 비동기 처리 설정 (@EnableAsync)
│   ├── ContextFilter.java               # GUID 컨텍스트 필터
│   └── RetryConfig.java                # 재시도 빈 설정
├── controller/
│   ├── BankingController.java            # 은행 API 컨트롤러
│   └── CustomerController.java           # 고객 관리 컨트롤러
├── domain/
│   ├── AccountStatus.java               # 계좌 상태 열거형
│   ├── AccountType.java                 # 계좌 유형 열거형
│   ├── Account.java                     # 계좌 엔티티
│   ├── AccountRequest.java              # 계좌 개설 요청 DTO
│   ├── TransactionStatus.java           # 거래 상태 열거형
│   ├── TransactionType.java             # 거래 유형 열거형
│   ├── Transaction.java                  # 거래 엔티티
│   ├── DepositRequest.java              # 입금 요청 DTO
│   ├── TransferRequest.java             # 이체 요청 DTO
│   ├── CustomerStatus.java             # 고객 상태 열거형
│   ├── CustomerType.java               # 고객 유형 열거형
│   ├── RiskLevel.java                  # 리스크 레벨 열거형
│   ├── Customer.java                   # 고객 엔티티
│   └── CustomerRequest.java            # 고객 등록 요청 DTO
├── repository/
│   ├── AccountRepository.java            # 계좌 MyBatis 매퍼
│   ├── CustomerRepository.java           # 고객 MyBatis 매퍼
│   ├── TransactionRepository.java        # 거래 MyBatis 매퍼
│   └── NotificationLogRepository.java   # 알림 로그 매퍼
├── service/
│   ├── AccountService.java             # 계좌 비즈니스 로직 (@Transactional)
│   ├── CustomerService.java            # 고객 비즈니스 로직 (@Transactional)
│   └── PaymentClient.java             # 외부 결제 시뮬레이션
├── event/
│   ├── TransactionFailed.java           # 거래 실패 이벤트
│   ├── TransactionFailureHandler.java    # 실패 이벤트 리스너 (@Async, AFTER_ROLLBACK)
│   └── OrderFailed.java              # 주문 실패 이벤트 (legacy)
├── retry/
│   ├── RetryStrategy.java             # 재시도 전략 인터페이스
│   ├── LinearBackoffRetryStrategy.java # 선형 증가 백오프 전략
│   ├── LockRetryTemplate.java         # 재시도 템플릿
│   └── RetryableException.java       # 재시도 가능 예외
├── exception/
│   ├── PaymentException.java          # 결제 관련 예외
│   └── OrderException.java           # 주문 관련 예외
└── util/
    ├── ContextHolder.java            # GUID 컨텍스트 관리
    ├── CtxMap.java                  # 컨텍스트 맵
    ├── GuidQueue.java               # GUID 큐
    └── GuidQueueUtil.java           # GUID 유틸리티

src/main/resources/
├── schema.sql                           # 데이터베이스 스키마
├── application.yml                      # 애플리케이션 설정
├── mapper/                             # MyBatis SQL 매핑
│   ├── AccountMapper.xml
│   ├── CustomerMapper.xml
│   ├── TransactionMapper.xml
│   └── NotificationLogMapper.xml
└── static/
    ├── banking.html                       # 메인 웹 페이지
    ├── banking-style.css                 # 은행 스타일
    └── banking-script.js                 # 프론트엔드 로직
```

## 🎨 핵심 디자인 패턴

### 1. Repository 패턴

```java
// AccountRepository.java
@Mapper
public interface AccountRepository {
    Account findById(Long id);
    void save(Account account);
    List<Account> findByCustomerId(Long customerId);
    void updateBalance(Account account);
}
```

**특징**:
- Repository 인터페이스와 MyBatis XML 매핑으로 데이터 접근 분리
- 단일 책임 원칙(SRP) 준수
- 테스트 용이성 확보

### 2. Service 패턴

```java
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher events;

    public Account createAccount(AccountRequest request) {
        // 비즈니스 로직 처리
        Account account = request.toAccount(accountNumber);
        accountRepository.save(account);

        // 초기 입금 처리
        Transaction transaction = Transaction.createDeposit(...);
        transactionRepository.save(transaction);

        try {
            paymentClient.processPayment(guid, transactionId, amount, forceFailure);
            account.deposit(amount);
            accountRepository.updateBalance(account);
            transaction.complete();
            return account;
        } catch (Exception e) {
            // 이벤트 발행 (롤백 후 처리)
            events.publishEvent(new TransactionFailed(..., transactionId, e.getMessage()));
            throw e;
        }
    }
}
```

**특징**:
- 비즈니스 로직을 서비스 레이어에서 캡슐화
- @Transactional로 선언적 트랜잭션 관리
- 이벤트 기반 롤백 후 처리

### 3. 전략 패턴 (Strategy Pattern)

```java
// RetryStrategy 인터페이스
public interface RetryStrategy {
    boolean shouldRetry(Exception e, int attemptCount);
    long getWaitTime(int attemptCount);
}

// 선형 증가 백오프 전략
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

**특징**:
- RetryStrategy 인터페이스로 다양한 재시도 전략 구현 가능
- 새로운 전략 추가가 용이한 확장성
- LinearBackoffRetryStrategy로 1000ms → 1500ms → 2000ms → ... 증가

### 4. 템플릿 메서드 패턴

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

**특징**:
- 재시도 로직 재사용
- Supplier<T>로 실행 로직 추상화
- 전략 패턴과 결합으로 유연성 확보

### 5. 이벤트 기반 아키텍처

```java
// 이벤트 발행
@Service
public class AccountService {
    private final ApplicationEventPublisher events;

    @Transactional
    public Transaction transfer(TransferRequest request) {
        try {
            // 이체 처리
            return transaction;
        } catch (Exception e) {
            // 이벤트 발행 (롤백 후 처리)
            events.publishEvent(new TransactionFailed(context, transactionId, e.getMessage()));
            throw e;
        }
    }
}

// 이벤트 리스너
@Component
@RequiredArgsConstructor
public class TransactionFailureHandler {
    private final NotificationService notifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Async
    public void handle(TransactionFailed event) {
        // 롤백 완료 후 비동기적으로 실행
        notifier.sendTransactionFailure(event.getTransactionId(), event.getReason());
    }
}
```

**특징**:
- Spring의 ApplicationEventPublisher로 이벤트 발행
- @TransactionalEventListener(phase = AFTER_ROLLBACK)로 롤백 후 처리
- @Async로 메인 스레드 블로킹 방지

### 6. 트랜잭션 관리

```java
// 기본 트랜잭션
@Transactional
public Transaction deposit(DepositRequest request) {
    // 동일 트랜잭션에서 실행
}

// 독립적 트랜잭션 (알림용)
@Service
public class NotificationService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendTransactionFailure(Long transactionId, String reason) {
        // 완전히 별도의 트랜잭션에서 실행
        // 메인 트랜잭션 결과와 상관없이 데이터 영속성 보장
    }
}
```

### 7. Context 전파 패턴

```java
// ContextHolder
public class ContextHolder {
    private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

    public static void initializeContext(String guid) {
        Map<String, Object> map = new HashMap<>();
        map.put("guid", guid);
        context.set(map);
    }

    public static void addClientInfo(String ip, String userAgent, String sessionId) {
        Map<String, Object> map = context.get();
        if (map != null) {
            map.put("clientIp", ip);
            map.put("userAgent", userAgent);
            map.put("sessionId", sessionId);
        }
    }

    public static Map<String, Object> copyContext() {
        return context.get();
    }

    public static void clearContext() {
        context.remove();
    }
}

// 사용 예시
@RestController
public class BankingController {
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(HttpServletRequest httpRequest) {
        String guid = guidQueueUtil.getGUID();
        ContextHolder.initializeContext(guid);

        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String sessionId = httpRequest.getSession().getId();
        ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

        try {
            // 비즈니스 로직
            return ResponseEntity.ok(...);
        } finally {
            ContextHolder.clearContext();
        }
    }
}
```

**특징**:
- ThreadLocal로 스레드 안전한 컨텍스트 관리
- GUID 기반 요청 추적
- IP, User-Agent, Session 정보 캡슈

## 🔄 트랜잭션 흐름 분석

### 성공 시나리오 (입금)

```
1. BankingController.deposit() 요청 수신
2. GUID 생성 및 Context 초기화
3. AccountService.deposit() 트랜잭션 시작
4. 계좌 확인 (status = ACTIVE 체크)
5. Transaction 생성 (status = PENDING)
6. PaymentClient.processPayment() 호출
   → LockRetryTemplate.execute() 실행
   → 결제 성공
7. 계좌 잔액 증가
8. 계좌 balance 업데이트
9. Transaction 상태 변경 (PENDING → COMPLETED)
10. 트랜잭션 커밋
11. Context 정리
```

### 실패 시나리오 (이체, 재시도 포함)

```
1. BankingController.transfer() 요청 수신
2. GUID 생성 및 Context 초기화
3. AccountService.transfer() 트랜잭션 시작
4. 출금/입금 계좌 확인
5. 출금 계좌 잔액 확인
6. Transaction 생성 (status = PENDING)
7. PaymentClient.processPayment() 호출
   → LockRetryTemplate.execute() 실행

   [재시도 1] 결제 실패 → 1000ms 대기 후 재시도
   [재시도 2] 결제 실패 → 1500ms 대기 후 재시도
   [재시도 3] 결제 실패 → 2000ms 대기 후 재시도
   [재시도 4] 결제 실패 → 2500ms 대기 후 재시도
   [재시도 5] 결제 최종 실패 → 예외 발생

8. TransactionFailed 이벤트 발행
9. 예외 재전달 → 트랜잭션 롤백으로 마크됨
10. 트랜잭션 롤백됨
    - 계좌 잔액 변경 롤백
    - Transaction 상태 롤백
11. TransactionFailureHandler.handle() 롤백 후 실행
12. 새로운 트랜잭션에서 알림 전송 (REQUIRES_NEW)
13. notification_logs 테이블에 실패 로그 저장
14. Context 정리
```

## 🧪 테스트 및 개발

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests AccountServiceTest

# 특정 테스트 메서드 실행
./gradlew test --tests AccountServiceTest.testCreateAccount
```

### 코드 품질

- **Lombok**: 보일러플레이트 코드 제거
- **MyBatis XML**: 복잡한 쿼리 분리 관리
- **열거형**: 타입 안전성 확보
- **Jakarta Validation**: 선언적 유효성 검사

### 개발 가이드라인

1. **규약 준수**:
   - Java 컨벤션 준수 (PascalCase 클래스, camelCase 메서드)
   - 네이밍 규칙 준수 (의미있는 변수명)

2. **커밋 메시지**:
   ```bash
   # Conventional Commits 사용
   feat: 계좌 이체 기능 추가
   fix: 입금 시 잔액 업데이트 버그 수정
   docs: README 업데이트
   refactor: 재시도 로직 리팩토링
   test: 계좌 서비스 단위 테스트 추가
   ```

3. **코드 리뷰**:
   - Pull Request 사용
   - 코드 리뷰 필수

4. **단위 테스트**:
   - 새로운 기능은 반드시 단위 테스트 작성
   - 테스트 커버리지 80% 이상 유지

## 🚢 배포 가이드

### 프로덕션 배포 준비

1. **데이터베이스 변경**:
   ```yaml
   # application-prod.yml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/banking
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
       driver-class-name: org.postgresql.Driver
   ```

2. **환경 변수 설정**:
   ```bash
   export DB_USERNAME=banking_user
   export DB_PASSWORD=secure_password
   export SPRING_PROFILES_ACTIVE=prod
   ```

3. **보안 설정**:
   - HTTPS 적용
   - 인증/권한 관리
   - CORS 설정
   - SQL Injection 방지 (MyBatis Prepared Statements)

4. **로깅 설정**:
   ```yaml
   logging:
     level:
       com.example.rollback: INFO
       org.springframework: WARN
     file:
       name: /var/log/banking/application.log
     pattern:
       file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
   ```

### Docker 배포

**Dockerfile**:
```dockerfile
# 빌드 단계
FROM gradle:7.5-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon

# 실행 단계
FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  banking-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/banking
      - SPRING_DATASOURCE_USERNAME=banking_user
      - SPRING_DATASOURCE_PASSWORD=secure_password
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - postgres

  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=banking
      - POSTGRES_USER=banking_user
      - POSTGRES_PASSWORD=secure_password
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres-data:
```

### 배포 명령어

```bash
# Docker 빌드
docker build -t banking-system .

# Docker 실행
docker run -p 8080:8080 banking-system

# Docker Compose 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f banking-app
```

## 🔧 문제 해결

### 일반적인 문제

#### 1. 포트 충돌

**문제**: `Port 8080 was already in use.`

**해결**:
```bash
# 8080 포트 사용 프로세스 확인
lsof -i :8080

# 또는 application.yml에서 포트 변경
server:
  port: 8081
```

#### 2. 데이터베이스 초기화

**문제**: 이전 데이터로 인해 테스트 실패

**해결**:
```bash
# 빌드 결과 제거
./gradlew clean

# 전체 빌드
./gradlew build
```

#### 3. GUID 큐 문제

**문제**: `HOSTNAME 환경변수를 찾을 수 없습니다.`

**해결**:
```bash
# Linux/Mac
export HOSTNAME=localhost

# Windows (PowerShell)
$env:HOSTNAME="localhost"

# 또는 application.yml에서 설정
guid:
  hostname: localhost
```

#### 4. H2 콘솔 접속 실패

**문제**: H2 콘솔 연결 안됨

**해결**:
```yaml
# application.yml에서 H2 콘솔 활성화 확인
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 로그 확인

```bash
# 애플리케이션 로그
tail -f application.log

# Gradle 빌드 로그
./gradlew build --info

# DEBUG 레벨 로깅
# application.yml
logging:
  level:
    com.example.rollback: DEBUG
```

## 📚 추가 리소스

### 학습 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [MyBatis 공식 문서](https://mybatis.org/mybatis-3/)
- [트랜잭션 관리 가이드](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [재시도 패턴](https://docs.microsoft.com/en-us/azure/architecture/patterns/retry)

### 관련 프로젝트

- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Batch](https://spring.io/projects/spring-batch)
- [Spring Security](https://spring.io/projects/spring-security)

## 🤝 기여

이 프로젝트는 교육용으로 제공됩니다. 자유롭게 다음을 수행할 수 있습니다:

- ✅ 다양한 트랜잭션 시나리오 실험
- ✅ 새로운 은행 기능 추가 (대출, 카드 등)
- ✅ 성능 최적화 및 인덱스 튜닝
- ✅ 다른 데이터베이스로 마이그레이션 (PostgreSQL, MySQL, Oracle)
- ✅ 추가적인 실패 처리 패턴 구현
- ✅ 단위 테스트 및 통합 테스트 작성
- ✅ CI/CD 파이프라인 구축
- ✅ 모니터링 및 로깅 시스템 강화

## 📄 라이선스

이 프로젝트는 교육 목적으로 제공됩니다.

**MIT License**

---

<div align="center">

**Made with ❤️ for Learning Enterprise Development**

[⬆ Back to Top](#-은행-계좌-관리-시스템)

</div>