package com.example.rollback.service;

import com.example.rollback.aop.MeasuredExecutionTime;
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

import java.util.concurrent.atomic.AtomicReference;

/**
 * 계좌 관련 핵심 로직(입금 등)을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    /** 계좌 저장소 */
    private final AccountRepository accountRepository;

    /** 거래 저장소 */
    private final TransactionRepository transactionRepository;

    /** 실패 이벤트 발행기 */
    private final ApplicationEventPublisher events;

    /** 락/경합 재시도 템플릿 */
    private final LockRetryTemplate lockRetryTemplate;

    /**
     * 입금을 처리합니다.
     */
    @Transactional
    @MeasuredExecutionTime("입금 처리")
    public Transaction deposit(DepositRequest request) {
        String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        AtomicReference<Long> lastTransactionId = new AtomicReference<>();

        lastTransactionId.set(null);

        Transaction transaction;
        log.info("입금 처리 시작 - 계좌ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

        if (request.isForceFailure()) {
            log.warn("DepositRequest의 forceFailure가 true로 설정되어 입금 처리를 강제로 실패시킵니다.");
            throw new RuntimeException("테스트를 위한 강제 입금 실패");
        }



        try {

        // 계좌 확인 (락/경합 시 재시도)
        Account account = lockRetryTemplate.execute(
                () -> loadAccountForDeposit(request.getAccountId()),
                (ex) -> events.publishEvent(new TransactionFailed(
                        ContextHolder.copyContext().asReadOnlyMap(),
                        lastTransactionId.get(),
                        ex.getClass().getSimpleName())));

        if (!account.isActive()) {
            throw new IllegalStateException("계좌가 활성 상태가 아닙니다");
        }
                    
            // 2. 거래 생성
            transaction = request.toTransaction(guid);
            transactionRepository.save(transaction);
            lastTransactionId.set(transaction.getId());
            log.info("거래 생성 완료 - 거래ID: {}", transaction.getId());

            // 3. 결제 연동(제거됨)

            // 4. 계좌 잔액 업데이트
            account.deposit(request.getAmount());
            accountRepository.updateBalance(account);

            // 5. 거래 완료 처리
            transaction.complete();
            transactionRepository.updateStatus(transaction.getId(), "COMPLETED");

            log.info("입금 처리 완료 - 계좌: {}, 금액: {}", account.getAccountNumber(), request.getAmount());
            return transaction;
        } catch (Exception ex) {
            events.publishEvent(new TransactionFailed(
                    ContextHolder.copyContext().asReadOnlyMap(),
                    lastTransactionId.get(),
                    ex.getClass().getSimpleName()));
            throw ex;
        }
    }

    private Account loadAccountForDeposit(Long accountId) {
        Account locked = accountRepository.findByIdForUpdateSkipLocked(accountId);
        if (locked != null) {
            return locked;
        }

        Account exists = accountRepository.findById(accountId);
        if (exists == null) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountId);
        }

        throw new RetryableException("account busy");
    }
}
