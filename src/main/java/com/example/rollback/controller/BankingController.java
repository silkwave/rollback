package com.example.rollback.controller;

import com.example.rollback.domain.Account;
import com.example.rollback.domain.AccountRequest;
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

// 은행 계좌 관련 REST API 컨트롤러
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking")
public class BankingController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationLogRepository notificationLogRepository;

    // 계좌 개설 엔드포인트
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.createAccount(request);
        log.info("계좌 개설 성공: {}", account.getAccountNumber());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", MDC.get("guid"),
                "message", "계좌가 성공적으로 개설되었습니다",
                "account", account));
    }

    // 입금 엔드포인트
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request) {
        Transaction transaction = accountService.deposit(request);
        log.info("입금 성공: {}", transaction.getReferenceNumber());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", MDC.get("guid"),
                "message", "입금이 성공적으로 처리되었습니다",
                "transaction", transaction));
    }

    // 전체 계좌 목록 조회
    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        log.info("모든 계좌 목록 조회");
        return accountRepository.findAll();
    }

    // 특정 계좌 조회
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

    // 고객별 계좌 조회
    @GetMapping("/accounts/customer/{customerId}")
    public List<Account> getAccountsByCustomerId(@PathVariable Long customerId) {
        log.info("고객별 계좌 목록 조회 - 고객ID: {}", customerId);
        return accountRepository.findByCustomerId(customerId);
    }

    // 전체 거래 내역 조회
    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        log.info("모든 거래 내역 조회");
        return transactionRepository.findAll();
    }

    // 전체 알림 로그 조회
    @GetMapping("/notifications")
    public List<NotificationLog> getAllNotifications() {
        log.info("모든 알림 로그 조회");
        return notificationLogRepository.findAll();
    }

    // 계좌 동결
    @PostMapping("/accounts/{id}/freeze")
    public ResponseEntity<?> freezeAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id);
        if (account == null) {
            log.warn("동결할 계좌를 찾을 수 없음: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        account.freeze();
        accountRepository.update(account);
        log.info("계좌 동결 성공: {}", account.getAccountNumber());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "계좌가 동결되었습니다",
            "account", account
        ));
    }

    // 계좌 활성화
    @PostMapping("/accounts/{id}/activate")
    public ResponseEntity<?> activateAccount(@PathVariable Long id) {
        Account account = accountRepository.findById(id);
        if (account == null) {
            log.warn("활성화할 계좌를 찾을 수 없음: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        account.activate();
        accountRepository.update(account);
        log.info("계좌 활성화 성공: {}", account.getAccountNumber());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "계좌가 활성화되었습니다",
            "account", account
        ));
    }
}