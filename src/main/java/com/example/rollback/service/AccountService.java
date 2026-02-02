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

/**
 * 은행 계좌 서비스 (핵심 트랜잭션 처리)
 * 
 * <p>계좌 개설, 입금, 계좌 상태 관리 등 계좌와 관련된 모든 비즈니스 로직을 처리합니다.
 * Spring의 트랜잭션 관리를 통해 ACID 속성을 보장하며, 실패 시 롤백과 이벤트 기반 알림을 처리합니다.
 * LockRetryTemplate을 사용하여 동시성 문제를 방지합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>계좌 개설 및 관리</li>
 *   <li>입금 처리 (외부 결제 게이트웨이 연동)</li>
 *   <li>계좌 상태 제어 (동결/활성화)</li>
 *   <li>트랜잭션 롤백 및 실패 알림</li>
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
     * 새로운 은행 계좌를 개설합니다.
     * 
     * <p>트랜잭션 내에서 계좌번호를 생성하고 계좌를 저장한 후, 초기 거래 기록을 생성합니다.
     * forceFailure가 true인 경우 트랜잭션을 강제로 롤백시켜 실패 시나리오를 시뮬레이션합니다.</p>
     * 
     * @param request 계좌 개설 요청 정보
     * @return 생성된 계좌 정보
     * @throws PaymentException forceFailure가 true인 경우 발생
     */
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

    /**
     * 계좌에 입금을 처리합니다.
     * 
     * <p>계좌를 확인하고, 거래를 생성한 후, 입금을 처리합니다.
     * LockRetryTemplate을 사용하여 동시성 문제를 방지합니다.
     * 입금이 실패할 경우 TransactionFailed 이벤트를 발행합니다.</p>
     * 
     * <p><strong>처리 순서:</strong><br>
     * 1. 계좌 존재 및 활성 상태 확인 (락으로 보호)<br>
     * 2. 거래 생성 및 저장<br>
     * 3. 계좌 잔액 증가<br>
     * 4. 거래 상태를 COMPLETED로 변경</p>
     * 
     * @param request 입금 요청 정보 (계좌ID, 금액, 통화 등)
     * @return 처리된 거래 정보
     * @throws IllegalArgumentException 계좌를 찾을 수 없는 경우
     * @throws IllegalStateException 계좌가 활성 상태가 아닌 경우
     * @throws RuntimeException 입금 처리 실패 시
     */
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