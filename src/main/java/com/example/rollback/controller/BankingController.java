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

/**
 * 은행 계좌 관련 REST API 컨트롤러
 * 
 * <p>계좌 개설, 입금, 계좌 상태 관리(동결/활성화), 거래 내역 조회 등
 * 은행 계좌와 관련된 모든 REST API 엔드포인트를 제공합니다.
 * 모든 요청은 GUID 기반으로 추적되며, 실패 시 트랜잭션 롤백과 이벤트 기반 알림이 처리됩니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>계좌 개설 및 관리</li>
 *   <li>입금 처리</li>
 *   <li>계좌 상태 제어 (동결/활성화)</li>
 *   <li>거래 내역 조회</li>
 *   <li>알림 로그 조회</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking")
public class BankingController {

    /** 계좌 서비스 - 계좌 관련 비즈니스 로직 처리 */
    private final AccountService accountService;
    
    /** 계좌 리포지토리 - 계좌 데이터 접근 */
    private final AccountRepository accountRepository;
    
    /** 거래 리포지토리 - 거래 내역 데이터 접근 */
    private final TransactionRepository transactionRepository;
    
    /** 알림 로그 리포지토리 - 알림 기록 데이터 접근 */
    private final NotificationLogRepository notificationLogRepository;

    /**
     * 새로운 은행 계좌를 개설하는 엔드포인트
     * 
     * @param request 계좌 개설 요청 정보 (고객ID, 계좌유형, 초기입금액 등)
     * @return 생성된 계좌 정보와 처리 결과
     */
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

    /**
     * 계좌에 입금하는 엔드포인트
     * 
     * @param request 입금 요청 정보 (계좌ID, 입금액, 통화 등)
     * @return 처리된 입금 거래 정보와 결과
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request) {
        Transaction transaction = accountService.deposit(request);
        log.info("입금 성공: {}", transaction.getGuid());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", MDC.get("guid"),
                "message", "입금이 성공적으로 처리되었습니다",
                "transaction", transaction));
    }

    /**
     * 모든 계좌 목록을 조회하는 엔드포인트
     * 
     * @return 전체 계좌 목록
     */
    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        log.info("모든 계좌 목록 조회");
        return accountRepository.findAll();
    }

    /**
     * 특정 ID의 계좌를 조회하는 엔드포인트
     * 
     * @param id 조회할 계좌의 ID
     * @return 계좌 정보 (존재하지 않는 경우 404)
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
     * 특정 고객의 모든 계좌를 조회하는 엔드포인트
     * 
     * @param customerId 고객 ID
     * @return 해당 고객의 계좌 목록
     */
    @GetMapping("/accounts/customer/{customerId}")
    public List<Account> getAccountsByCustomerId(@PathVariable Long customerId) {
        log.info("고객별 계좌 목록 조회 - 고객ID: {}", customerId);
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * 모든 거래 내역을 조회하는 엔드포인트
     * 
     * @return 전체 거래 내역 목록
     */
    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        log.info("모든 거래 내역 조회");
        return transactionRepository.findAll();
    }

    /**
     * 모든 알림 로그를 조회하는 엔드포인트
     * 
     * @return 전체 알림 로그 목록
     */
    @GetMapping("/notifications")
    public List<NotificationLog> getAllNotifications() {
        log.info("모든 알림 로그 조회");
        return notificationLogRepository.findAll();
    }

    /**
     * 계좌를 동결 상태로 변경하는 엔드포인트
     * 
     * @param id 동결할 계좌의 ID
     * @return 처리 결과와 업데이트된 계좌 정보
     */
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
            "guid", MDC.get("guid"),
            "message", "계좌가 동결되었습니다",
            "account", account
        ));
    }

    /**
     * 동결된 계좌를 활성 상태로 변경하는 엔드포인트
     * 
     * @param id 활성화할 계좌의 ID
     * @return 처리 결과와 업데이트된 계좌 정보
     */
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
            "guid", MDC.get("guid"),
            "message", "계좌가 활성화되었습니다",
            "account", account
        ));
    }
}