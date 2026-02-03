# 코드 리팩토링 계획

## 목표

기능을 변경하지 않고 코드 길이를 단축하고 가독성을 향상시킵니다.

## 주요 리팩토링 대상

### 1. Account.java (249→~180 lines)
- **중복 로깅 패턴 추출**: `logAccountAction()` 유틸리티 메서드
- **메서드 체이닝 제거**: 명확한 절차적 코드로 변경
- **유효성 검사 로직 통합**: `validateAmount()` 중앙 집중화
- **팩토리 메서드 개선**: `getOverdraftLimit()` 상수 추출

### 2. AccountService.java (201→~150 lines)
- **공통 타이머 유틸리티 추출**: `measureTime()` 메서드
- **복잡한 메서드 분해**: `createAccount()` → 작은 책임 단위
- **에러 처리 통합**: `handleTransactionFailure()` 중앙 집중화

### 3. BankingController.java (210→~160 lines)
- **공통 응답 생성기 추출**: `createSuccessResponse()` 메서드
- **중복된 상태 변경 로직 추출**: `changeAccountStatus()` 유틸리티
- **간결한 엔드포인트**: 람다 표현식 활용

### 4. Transaction.java (240→~180 lines)
- **공통 거래 생성 로직 추출**: `createBaseTransaction()` 팩토리
- **상태 변경 메서드 통합**: `updateStatus()` → 단일 진입점
- **간결한 팩토리 메서드**: 명확한 파라미터 전달

## 예상 효과

| 파일 | 리팩토링 전 | 리팩토링 후 | 감소율 |
|------|------------|------------|--------|
| Account.java | 249 lines | ~180 lines | 28% |
| AccountService.java | 201 lines | ~150 lines | 25% |
| BankingController.java | 210 lines | ~160 lines | 24% |
| Transaction.java | 240 lines | ~180 lines | 25% |
| **전체 감소량** | - | ~100+ lines | **~25%** |

## 리팩토링 원칙

1. **기능 보존**: 모든 비즈니스 로직은 그대로 유지
2. **주석 보존**: 모든 Javadoc과 설명은 그대로 유지
3. **가독성 향상**: 복잡한 중첩 구조 → 단순한 절차적 코드
4. **중복 제거**: 공통 패턴은 유틸리티 메서드로 추출
5. **현대적 문법**: Java 21+ 기능 적극 활용
6. **일관성**: 모든 클래스에 동일한 패턴 적용

## 우선순위

1. **높음**: AccountService.java, BankingController.java (가장 복잡한 부분)
2. **중간**: Account.java, Transaction.java (도메인 로직)
3. **낮음**: 유틸리티 클래스, 설정 클래스

## 리팩토링 전략

### 1. 공통 패턴 식별
- 반복되는 로깅 패턴
- 중복된 예외 처리
- 비슷한 응답 생성 구조
- 공통된 유효성 검사 로직

### 2. 유틸리티 메서드 추출
- 로깅 헬퍼 메서드
- 응답 생성기
- 에러 처리기
- 타이머 유틸리티

### 3. 메서드 분해
- 단일 책임 원칙 적용
- 복잡한 조건문 → 작은 메서드
- 긴 메서드 → 의미 있는 단위

### 4. 현대적 문법 적용
- 람다 표현식
- 메서드 참조
- 스트림 API
- 레코드 타입 (가능한 경우)

이 계획을 실행하면 코드 길이는 약 25% 단축되고, 가독성과 유지보수성이 크게 향상될 것입니다.
