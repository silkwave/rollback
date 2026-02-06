# 🏦 Spring Boot 뱅킹 시스템 (트랜잭션 롤백)

## 프로젝트 개요

이 프로젝트는 Spring Boot 3.2.0 및 Java 21로 구축된 포괄적인 뱅킹 시스템 데모입니다. 주로 다음을 시연합니다:
- **트랜잭션 관리 및 롤백 처리:** `@Transactional`을 통한 ACID 속성 준수 및 이벤트 기반 롤백 처리.
- **이벤트 기반 아키텍처:** `TransactionFailed` 이벤트 및 `@TransactionalEventListener`를 활용한 롤백 후 알림.
- **설정 가능한 재시도 메커니즘:** 비관적 락 실패 및 데드락 처리를 위한 `RandomBackoffRetryStrategy`와 같은 유연한 재시도 로직을 위한 전략 및 컴포지트 패턴 구현.
- **요청 컨텍스트 추적:** `ThreadLocal` 및 MDC와 함께 GUID를 사용하여 요청을 추적하고 스레드 간 컨텍스트 전파.
- **비동기 처리:** 커스텀 스레드 풀 및 컨텍스트 전파를 통해 `@EnableAsync`를 활용한 비블로킹 작업.
- **명확한 관심사 분리:** MVC 패턴 준수 및 전역 예외 처리.
- **코드 품질:** 코드 중복 감소 및 유지보수성 향상에 중점.

이 시스템은 계좌 관리, 트랜잭션 처리(입금, 이체), 고객 관리 및 비동기 알림 시스템을 제공합니다.

## 빌드 및 실행

이 프로젝트는 Gradle을 사용하여 빌드됩니다.

### 필수 조건
- Java 21 이상
- Gradle 8.x 이상

### 명령어

**1. 프로젝트 빌드:**
```bash
./gradlew build
```

**2. 애플리케이션 실행:**
```bash
./gradlew bootRun
# 또는
java -jar build/libs/*.jar
```

**3. 애플리케이션 접속:**
- **웹 UI**: `http://localhost:8080`
- **API 기본 경로**: `http://localhost:8080/api/banking`
- **H2 콘솔 (개발용)**: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:testdb`
    - 사용자명: `sa`
    - 비밀번호: (없음)

## 개발 규칙

### 1. 트랜잭션 관리
- Spring의 `@Transactional` 어노테이션을 사용하여 ACID 준수.
- `@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)`은 트랜잭션이 롤백된 후 작업을 트리거하여 이벤트 기반 실패 알림을 가능하게 합니다.

### 2. 재시도 메커니즘
- **전략 패턴:** `LockRetryTemplate`은 비즈니스 코드와 분리된 재시도 로직을 캡슐화합니다.
- **`RandomBackoffRetryStrategy`:** 동시 재시도 중 "Thundering Herd" 문제를 방지하기 위해 랜덤 백오프 알고리즘(지터 포함)을 사용합니다.
- **`RetryCondition` (컴포지트 패턴):** 락 실패, 데드락 등에 대한 재시도 조건을 동적으로 조합할 수 있습니다.

### 3. 요청 컨텍스트 및 로깅
- **GUID 기반 추적:** 각 요청에는 고유한 GUID가 할당됩니다.
- **`ThreadLocal` 컨텍스트:** 요청별 컨텍스트는 `ThreadLocal`에 저장됩니다.
- **MDC (Mapped Diagnostic Context):** `ContextFilter`를 통해 GUID 및 클라이언트 정보(IP, User-Agent, 세션 ID)가 MDC를 통해 로그로 전파됩니다.
- **`ContextFilter`:** 모든 HTTP 요청에 대해 GUID 생성, MDC 설정, 클라이언트 정보 추출, 컨텍스트 정리 작업을 관리합니다.

### 4. 비동기 작업
- **`@EnableAsync`:** 커스텀 스레드 풀(2-5개 스레드)을 사용하여 Spring의 비동기 기능을 활성화합니다.
- **컨텍스트 전파:** 요청 컨텍스트(GUID, MDC)가 비동기 스레드로 올바르게 전파되도록 합니다.
- **오류 처리:** 비동기 작업의 견고한 오류 처리를 위해 `AsyncUncaughtExceptionHandler`가 구성됩니다.

### 5. 예외 처리
- **`GlobalExceptionHandler`:** `@RestControllerAdvice`를 통한 중앙 집중식 예외 처리로, 표준화된 JSON 오류 응답을 제공하고 컨트롤러의 반복적인 코드 작성을 줄입니다.

### 6. 코드 구조 및 모범 사례
- 명확한 MVC 분리.
- Lombok을 사용하여 반복적인 코드 감소.
- XML 매퍼를 통해 구성된 MyBatis를 ORM으로 사용.
- 개발용 H2 인메모리 데이터베이스 사용.

## 롤백 동작 테스트
- **웹 UI:** "실패 강제" 체크박스를 활성화합니다.
- **API 요청:** 요청 본문에 `"forceFailure": true`를 설정합니다.
- 이를 통해 트랜잭션 실패, 롤백 및 후속 알림 처리를 관찰할 수 있습니다.