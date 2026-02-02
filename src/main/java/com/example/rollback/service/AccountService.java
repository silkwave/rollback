package com.example.rollback.service;

import com.example.rollback.domain.Account;
import com.example.rollback.domain.AccountRequest;
import com.example.rollback.domain.DepositRequest;
import com.example.rollback.domain.Transaction;
import com.example.rollback.event.TransactionFailed;
import com.example.rollback.repository.AccountRepository;
import com.example.rollback.repository.TransactionRepository;
import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.rollback.retry.LockRetryTemplate;

import java.math.BigDecimal;

// 은행 계좌 서비스 (핵심 트랜잭션 처리)
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher events;
    private final LockRetryTemplate lockRetryTemplate;

    // 계좌 개설
    @Transactional
    public Account createAccount(AccountRequest request) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        try {
            log.info("계좌 개설 시작 - 고객ID: {}, 유형: {}", request.getCustomerId(), request.getAccountType());

            // 1. 계좌번호 생성
            String accountNumber = IdGenerator.generate("ACC");
            log.info("계좌번호 생성: {}", accountNumber);

            // 2. 계좌 생성
            Account account = request.toAccount(accountNumber);
            accountRepository.save(account);
            log.info("계좌 저장 완료 - ID: {}", account.getId());

            // 3. 초기 입금 처리
            if (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
                Transaction transaction = Transaction.createDeposit(
                        guid, account.getId(), request.getCustomerId(),
                        request.getInitialDeposit(), request.getCurrency(), "초기입금");
                transactionRepository.save(transaction);

                try {
                    // 이전에 paymentClient.processPayment 로직이 있었으나 제거됨
                    account.deposit(request.getInitialDeposit());
                    accountRepository.updateBalance(account);
                    transaction.complete();
                    transactionRepository.updateStatus(transaction.getId(), "COMPLETED");

                    log.info("초기 입금 완료 - 계좌: {}, 금액: {}", accountNumber, request.getInitialDeposit());
                } catch (Exception e) {
                    log.error("초기 입금 실패 - 계좌: {}, 사유: {}", accountNumber, e.getMessage(), e);
                    transaction.fail("초기 입금 처리 실패: " + e.getMessage());
                    transactionRepository.updateStatus(transaction.getId(), "FAILED");

                    // 롤백 후 이벤트 발행
                    events.publishEvent(new TransactionFailed(ContextHolder.copyContext().asReadOnlyMap(),
                            transaction.getId(), e.getMessage()));
                    throw e;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("계좌 개설 완료 - 계좌번호: {}, 소요시간: {}ms", accountNumber, duration);
            return account;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("계좌 개설 실패 - 소요시간: {}ms, 사유: {}", duration, e.getMessage(), e);
            throw new RuntimeException("계좌 개설에 실패했습니다.", e);
        }
    }

    // 입금 처리
    @Transactional
    public Transaction deposit(DepositRequest request) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        try {
            log.info("입금 처리 시작 - 계좌ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

            // 1. 계좌 확인
            Account account = lockRetryTemplate.execute(() -> 
                accountRepository.findById(request.getAccountId())
            );
            if (account == null) {
                throw new IllegalArgumentException("계좌를 찾을 수 없습니다: " + request.getAccountId());
            }

            if (!account.isActive()) {
                throw new IllegalStateException("계좌가 활성 상태가 아닙니다");
            }

            // 2. 거래 생성
            Transaction transaction = request.toTransaction(guid);
            transaction.generateReferenceNumber();
            transactionRepository.save(transaction);
            log.info("거래 생성 완료 - 거래ID: {}", transaction.getId());

            // 3. 결제 처리
            try {
                // 이전에 paymentClient.processPayment 로직이 있었으나 제거됨

                // 4. 계좌 잔액 업데이트
                account.deposit(request.getAmount());
                accountRepository.updateBalance(account);

                // 5. 거래 완료 처리
                transaction.complete();
                transactionRepository.updateStatus(transaction.getId(), "COMPLETED");

                long duration = System.currentTimeMillis() - startTime;
                log.info("입금 처리 완료 - 계좌: {}, 금액: {}, 소요시간: {}ms",
                        account.getAccountNumber(), request.getAmount(), duration);
                return transaction;

            } catch (Exception e) {
                log.error("입금 처리 실패 - 거래ID: {}, 사유: {}", transaction.getId(), e.getMessage(), e);
                events.publishEvent(new TransactionFailed(ContextHolder.copyContext().asReadOnlyMap(),
                        transaction.getId(), e.getMessage()));
                throw e;
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("입금 처리 실패 - 소요시간: {}ms, 사유: {}", duration, e.getMessage(), e);
            throw new RuntimeException("입금 처리에 실패했습니다.", e);
        }
    }

}