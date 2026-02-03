# AGENTS.md - 개발 에이전트 가이드

## 빌드/테스트/실행 명령어

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행 (개발)
./gradlew bootRun

# 테스트 전체 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "AccountServiceTest"

# 특정 테스트 메서드 실행
./gradlew test --tests "AccountServiceTest.createAccount_shouldSucceed"

# 클린 빌드
./gradlew clean build

# JAR 파일 직접 실행
java -jar build/libs/*.jar
```

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.2.0
- **MyBatis**: 3.0.3
- **H2 Database**: 인메모리 개발용 DB
- **Lombok**: 보일러플레이트 코드 감소
- **Gradle**: 8.x

## 코드 스타일 가이드라인

### 1. 임포트 순서

```java
// 1. org.springframework.*
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

// 2. com.example.rollback.*
import com.example.rollback.domain.Account;
import com.example.rollback.service.AccountService;

// 3. java.*
import java.util.List;
import java.time.LocalDateTime;
```

### 2. Lombok 어노테이션

- **@Data**: 도메인 엔티티 클래스에 사용
- **@RequiredArgsConstructor**: final 필드가 있는 클래스에 사용 (의존성 주입용)
- **@Slf4j**: 로깅이 필요한 클래스에 사용 (log.XXX 형태로 사용)

### 3. 클래스 및 메서드 문서화

모든 public 클래스와 메서드에 Javadoc 작성 (HTML 태그 허용):

```java
/**
 * 은행 계좌 서비스
 * 
 * <p>계좌 개설, 입금 등의 비즈니스 로직을 처리합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>계좌 개설</li>
 *   <li>입금 처리</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
```

### 4. 의존성 주입

생성자 주입 사용, 모든 의존성은 `final`로 선언:

```java
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher events;
}
```

### 5. 트랜잭션 관리

서비스 메서드에 `@Transactional` 사용, 실패 시 이벤트 발행:

```java
@Transactional
public Account createAccount(AccountRequest request) {
    try {
        // 비즈니스 로직
    } catch (Exception e) {
        events.publishEvent(new TransactionFailed(context, transactionId, e.getMessage()));
        throw e;
    }
}
```

### 6. 로깅

SLF4J + MDC 사용, 모든 로그에 GUID 포함:

```java
// MDC에 GUID 설정
MDC.put("guid", guid);

// 로깅 (한국어 메시지)
log.info("계좌 개설 성공: {}", account.getAccountNumber());
log.error("처리 실패: {}", e.getMessage(), e);
```

### 7. 에러 처리

GlobalExceptionHandler에서 중앙 처리, 커스텀 예외 사용:

```java
// 비즈니스 예외
throw new IllegalArgumentException("잘못된 요청입니다");
throw new IllegalStateException("계좌가 활성 상태가 아닙니다");

// 커스텀 예외
throw new PaymentException("결제 처리 실패");
```

### 8. API 응답 형식

일관된 JSON 응답 구조 사용:

```java
return ResponseEntity.ok(Map.of(
    "success", true,
    "guid", MDC.get("guid"),
    "message", "처리가 완료되었습니다",
    "data", result
));
```

### 9. 네이밍 컨벤션

- **클래스명**: PascalCase (AccountService, TransactionFailed)
- **메서드명**: camelCase (createAccount, deposit)
- **상수명**: UPPER_SNAKE_CASE (MAX_RETRIES)
- **필드명**: camelCase (accountNumber, createdAt)
- **엔드포인트**: /api/banking/accounts, /api/banking/deposit

### 10. 도메인 패키지 구조

```
com.example.rollback/
├── controller/      # REST API 엔드포인트
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층 (MyBatis)
├── domain/          # 엔티티, DTO, Enum
├── event/           # 이벤트 클래스 및 핸들러
├── retry/           # 재시도 메커니즘 (Strategy 패턴)
├── util/            # 유틸리티 클래스
├── config/          # 설정 클래스
└── exception/       # 예외 클래스 및 핸들러
```

## 주의사항

- **GUID 관리**: 모든 요청은 ContextHolder에서 GUID를 가져와 MDC에 설정
- **ThreadLocal**: ContextHolder는 ThreadLocal 기반, 요청 완료 후 자동 정리됨
- **비동기 처리**: @Async 메서드는 컨텍스트 전파 확인 필요
- **재시도 로직**: LockRetryTemplate 사용, 별도의 try-catch 불필요
- **리포지토리**: MyBatis 사용, SQL은 resources/mapper/*.xml에 작성
