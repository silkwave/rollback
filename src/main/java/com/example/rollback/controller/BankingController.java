package com.example.rollback.controller;

import com.example.rollback.domain.Account;
import com.example.rollback.domain.DepositRequest;
import com.example.rollback.domain.NotificationLog;
import com.example.rollback.domain.Transaction;
import com.example.rollback.service.AccountService;
import com.example.rollback.repository.AccountRepository;
import com.example.rollback.repository.NotificationLogRepository;
import com.example.rollback.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 뱅킹 API(계좌/거래/알림)를 제공합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking")
public class BankingController {

    /** 계좌 서비스 */
    private final AccountService accountService;
    
    /** 계좌 조회용 리포지토리 */
    private final AccountRepository accountRepository;
    
    /** 거래 조회용 리포지토리 */
    private final TransactionRepository transactionRepository;
    
    /** 알림 로그 조회용 리포지토리 */
    private final NotificationLogRepository notificationLogRepository;

    /**
     * 입금을 처리합니다.
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request) {
        Transaction transaction = accountService.deposit(request);
        log.info("입금 성공: {}", transaction.getGuid());

        return createSuccessResponse("입금이 성공적으로 처리되었습니다", "transaction", transaction);
    }

    /**
     * 계좌 목록을 조회합니다.
     */
    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        log.info("모든 계좌 목록 조회");
        return accountRepository.findAll();
    }

    /**
     * 계좌 단건을 조회합니다.
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id);
        if (account != null) {
            log.info("계좌 조회 성공: {}", account.getAccountNumber());
            return ResponseEntity.ok(account);
        } else {
            log.warn("계좌를 찾을 수 없음: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 고객별 계좌를 조회합니다.
     */
    @GetMapping("/accounts/customer/{customerId}")
    public List<Account> getAccountsByCustomerId(@PathVariable Long customerId) {
        log.info("고객별 계좌 목록 조회 - 고객ID: {}", customerId);
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * 거래 내역을 조회합니다.
     */
    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        log.info("모든 거래 내역 조회");
        return transactionRepository.findAll();
    }

    /**
     * 알림 로그를 조회합니다.
     */
    @GetMapping("/notifications")
    public List<NotificationLog> getAllNotifications() {
        log.info("모든 알림 로그 조회");
        return notificationLogRepository.findAll();
    }

    /**
     * 계좌를 동결합니다.
     */
    @PostMapping("/accounts/{id}/freeze")
    public ResponseEntity<?> freezeAccount(@PathVariable Long id) {
        return changeAccountStatus(id, Account::freeze, "동결", "계좌가 동결되었습니다");
    }

    /**
     * 계좌를 활성화합니다.
     */
    @PostMapping("/accounts/{id}/activate")
    public ResponseEntity<?> activateAccount(@PathVariable Long id) {
        return changeAccountStatus(id, Account::activate, "활성화", "계좌가 활성화되었습니다");
    }

    /**
     * 성공 응답 바디를 생성합니다.
     */
    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message, String dataKey, Object data) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", MDC.get("guid"),
                "message", message,
                dataKey, data
        ));
    }

    /**
     * 계좌 상태를 변경합니다.
     */
    private ResponseEntity<?> changeAccountStatus(Long id, Consumer<Account> accountAction, String actionLog,
            String successMessage) {
        Account account = accountRepository.findById(id);
        if (account == null) {
            log.warn("계좌를 찾을 수 없음: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        accountAction.accept(account);
        accountRepository.update(account);
        log.info("계좌 {} 성공: {}", actionLog, account.getAccountNumber());
        
        return createSuccessResponse(successMessage, "account", account);
    }
}
