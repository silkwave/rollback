package com.example.rollback.service;

import com.example.rollback.domain.Account;
import com.example.rollback.domain.DepositRequest;
import com.example.rollback.domain.Transaction;
import com.example.rollback.event.TransactionFailed;
import com.example.rollback.repository.AccountRepository;
import com.example.rollback.repository.TransactionRepository;
import com.example.rollback.retry.RetryableException;
import com.example.rollback.util.ContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.rollback.retry.LockRetryTemplate;

/**
 * 은행 계좌 서비스 (핵심 트랜잭션 처리)
 * 
 * <p>
 * 입금, 계좌 상태 관리 등 계좌와 관련된 모든 비즈니스 로직을 처리합니다.
 * Spring의 트랜잭션 관리를 통해 ACID 속성을 보장하며, 실패 시 롤백과 이벤트 기반 알림을 처리합니다.
 * LockRetryTemplate을 사용하여 동시성 문제를 방지합니다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 * <li>입금 처리 (외부 결제 게이트웨이 연동)</li>
 * <li>계좌 상태 제어 (동결/활성화)</li>
 * <li>트랜잭션 롤백 및 실패 알림</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    /** 계좌 리포지토리 - 계좌 데이터 접근 */
    private final AccountRepository accountRepository;

    /** 거래 리포지토리 - 거래 내역 데이터 접근 */
    private final TransactionRepository transactionRepository;

    /** 이벤트 발행기 - 트랜잭션 실패 시 이벤트 발행 */
    private final ApplicationEventPublisher events;

    /** 락 재시도 템플릿 - 동시성 제어 및 재시도 처리 */
    private final LockRetryTemplate lockRetryTemplate;

    /**
     * 계좌에 입금을 처리합니다.
     * 
     * <p>
     * 계좌를 확인하고, 거래를 생성한 후, 입금을 처리합니다.
     * LockRetryTemplate을 사용하여 동시성 문제를 방지합니다.
     * 입금이 실패할 경우 TransactionFailed 이벤트를 발행합니다.
     * </p>
     * 
     * <p>
     * <strong>처리 순서:</strong><br>
     * 1. 계좌 존재 및 활성 상태 확인 (락으로 보호)<br>
     * 2. 거래 생성 및 저장<br>
     * 3. 계좌 잔액 증가<br>
     * 4. 거래 상태를 COMPLETED로 변경
     * </p>
     * 
     * @param request 입금 요청 정보 (계좌ID, 금액, 통화 등)
     * @return 처리된 거래 정보
     * @throws IllegalArgumentException 계좌를 찾을 수 없는 경우
     * @throws IllegalStateException    계좌가 활성 상태가 아닌 경우
     * @throws RuntimeException         입금 처리 실패 시
     */
    @Transactional    
    public Transaction deposit(DepositRequest request) {
        String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        java.util.concurrent.atomic.AtomicReference<Long> lastTransactionId = new java.util.concurrent.atomic.AtomicReference<>();

        return measureExecutionTime(() -> lockRetryTemplate.execute(() -> {
            lastTransactionId.set(null);

            Transaction transaction;
            log.info("입금 처리 시작 - 계좌ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

            if (request.isForceFailure()) {
                log.warn("DepositRequest의 forceFailure가 true로 설정되어 입금 처리를 강제로 실패시킵니다.");
                throw new RuntimeException("테스트를 위한 강제 입금 실패");
            }

            // 1. 계좌 확인 (락 획득 시도)
            Account account = accountRepository.findByIdForUpdateSkipLocked(request.getAccountId());
            if (account == null) {
                Account exists = accountRepository.findById(request.getAccountId());
                if (exists == null) {
                    throw new IllegalArgumentException("계좌를 찾을 수 없습니다: " + request.getAccountId());
                }
                throw new RetryableException("account busy");
            }

            if (!account.isActive()) {
                throw new IllegalStateException("계좌가 활성 상태가 아닙니다");
            }

            // 2. 거래 생성
            transaction = request.toTransaction(guid);
            transactionRepository.save(transaction);
            lastTransactionId.set(transaction.getId());
            log.info("거래 생성 완료 - 거래ID: {}", transaction.getId());

            // 3. 결제 처리
            // 이전에 paymentClient.processPayment 로직이 있었으나 제거됨

            // 4. 계좌 잔액 업데이트
            account.deposit(request.getAmount());
            accountRepository.updateBalance(account);

            // 5. 거래 완료 처리
            transaction.complete();
            transactionRepository.updateStatus(transaction.getId(), "COMPLETED");

            log.info("입금 처리 완료 - 계좌: {}, 금액: {}", account.getAccountNumber(), request.getAmount());
            return transaction;
        }, (ex) -> {
            Long txId = lastTransactionId.get();
            events.publishEvent(new TransactionFailed(
                    ContextHolder.copyContext().asReadOnlyMap(),
                    txId,
                    ex.getClass().getSimpleName()));
        }), "입금 처리");
    }

    /**
     * 주어진 작업을 실행하고 실행 시간을 측정하여 로깅합니다.
     * 
     * @param <T>      작업의 반환 타입
     * @param task     실행할 작업 (Supplier 형태)
     * @param taskName 로깅에 사용될 작업의 이름
     * @return 작업의 결과
     * @throws Exception 작업 실행 중 발생한 예외
     */
    private <T> T measureExecutionTime(java.util.function.Supplier<T> task, String taskName) {
        long startTime = System.currentTimeMillis();
        try {
            T result = task.get();
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} 완료 - 소요시간: {}ms", taskName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} 실패 - 소요시간: {}ms", taskName, duration, ex);
            throw new RuntimeException(ex); // Supplier가 Exception을 던질 수 없으므로 RuntimeException으로 래핑
        }
    }

    /**
     * 트랜잭션 처리 실패 시 공통 에러 처리 로직을 수행합니다.
     * 에러를 로깅하고, TransactionFailed 이벤트를 발행하며, 런타임 예외를 발생시킵니다.
     *
     * @param transaction     실패한 트랜잭션 객체
     * @param e               발생한 예외
     * @param contextMessage  실패 시 로깅될 메시지
     * @param transactionType 실패한 트랜잭션의 타입 (예: "입금", "초기 입금")
     * @throws RuntimeException 트랜잭션 실패를 알리는 런타임 예외
     */
    // NOTE:
    // 실패 이벤트(TransactionFailed)는 LockRetryTemplate의 "최종 실패 훅(onFinalFailure)"에서
    // 1회만 발행되도록 처리합니다. 재시도 중간 실패에서는 이벤트를 발행하지 않습니다.
}
