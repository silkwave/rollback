package com.example.rollback.controller;

import com.example.rollback.domain.Account;
import com.example.rollback.domain.AccountRequest;
import com.example.rollback.domain.DepositRequest;
import com.example.rollback.domain.TransferRequest;
import com.example.rollback.domain.Transaction;
import com.example.rollback.service.AccountService;
import com.example.rollback.repository.AccountRepository;
import com.example.rollback.repository.TransactionRepository;
import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.GuidQueueUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.slf4j.MDC;

// 은행 계좌 관련 REST API 컨트롤러
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking")
public class BankingController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final GuidQueueUtil guidQueueUtil;

    // 계좌 개설 엔드포인트
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountRequest request, BindingResult bindingResult,
                                     HttpServletRequest httpRequest) {
        String guid = setupRequestContext(httpRequest, "POST /api/banking/accounts - 계좌 개설 요청: " + request);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Account account = accountService.createAccount(request);
            log.info("계좌 개설 성공: {}", account.getAccountNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "계좌가 성공적으로 개설되었습니다",
                    "account", account));

        } catch (Exception e) {
            log.error("계좌 개설 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "계좌 개설 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 입금 엔드포인트
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request, BindingResult bindingResult,
                                HttpServletRequest httpRequest) {
        String guid = setupRequestContext(httpRequest, "POST /api/banking/deposit - 입금 요청: " + request);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Transaction transaction = accountService.deposit(request);
            log.info("입금 성공: {}", transaction.getReferenceNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "입금이 성공적으로 처리되었습니다",
                    "transaction", transaction));

        } catch (Exception e) {
            log.error("입금 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "입금 처리 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 이체 엔드포인트
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request, BindingResult bindingResult,
                                 HttpServletRequest httpRequest) {
        String guid = setupRequestContext(httpRequest, "POST /api/banking/transfer - 이체 요청: " + request);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Transaction transaction = accountService.transfer(request);
            log.info("이체 성공: {}", transaction.getReferenceNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "이체가 성공적으로 처리되었습니다",
                    "transaction", transaction));

        } catch (Exception e) {
            log.error("이체 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이체 처리 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 전체 계좌 목록 조회
    @GetMapping("/accounts")
    public List<Account> getAllAccounts(HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/banking/accounts - 모든 계좌 조회 요청", httpRequest);
        try {
            log.info("모든 계좌 목록 조회");
            return accountRepository.findAll();

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 특정 계좌 조회
    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id, HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/banking/accounts/" + id + " - 계좌 조회 요청", httpRequest);
        try {
            Account account = accountRepository.findById(id);

            if (account != null) {
                log.info("계좌 조회 성공: {}", account.getAccountNumber());
                return ResponseEntity.ok(account);
            } else {
                log.warn("계좌를 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 고객별 계좌 조회
    @GetMapping("/accounts/customer/{customerId}")
    public List<Account> getAccountsByCustomerId(@PathVariable Long customerId, HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/banking/accounts/customer/" + customerId + " - 고객별 계좌 조회 요청", httpRequest);
        try {
            log.info("고객별 계좌 목록 조회 - 고객ID: {}", customerId);
            return accountRepository.findByCustomerId(customerId);

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 전체 거래 내역 조회
    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions(HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/banking/transactions - 모든 거래 내역 조회 요청", httpRequest);
        try {
            log.info("모든 거래 내역 조회");
            return transactionRepository.findAll();

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    private String setupRequestContext(HttpServletRequest httpRequest, String operationMessage) {
        String guid = guidQueueUtil.getGUID();
        ContextHolder.initializeContext(guid);
        MDC.put("guid", guid);

        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String sessionId = httpRequest.getSession().getId();

        ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

        log.info("=======================================================");
        log.info("{}", operationMessage);
        return guid;
    }

    private String initializeContextAndLog(String operation, HttpServletRequest httpRequest) {
        String guid = guidQueueUtil.getGUID();
        ContextHolder.initializeContext(guid);
        MDC.put("guid", guid);

        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String sessionId = httpRequest.getSession().getId();

        ContextHolder.addClientInfo(clientIp, userAgent, sessionId);

        log.info("=======================================================");
        log.info("{}", operation);
        return guid;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }
}