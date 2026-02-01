package com.example.rollback.controller;

import com.example.rollback.domain.Customer;
import com.example.rollback.domain.CustomerRequest;
import com.example.rollback.service.CustomerService;
import com.example.rollback.repository.CustomerRepository;
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

// 고객 관련 REST API 컨트롤러
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final GuidQueueUtil guidQueueUtil;

    // 고객 생성 엔드포인트
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request, BindingResult bindingResult,
                                       HttpServletRequest httpRequest) {
        String guid = setupRequestContext(httpRequest, "POST /api/banking/customers - 고객 생성 요청: " + request);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Customer customer = customerService.createCustomer(request);
            log.info("고객 생성 성공: {}", customer.getCustomerNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "고객이 성공적으로 생성되었습니다",
                    "customer", customer));

        } catch (Exception e) {
            log.error("고객 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "고객 생성 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 전체 고객 목록 조회
    @GetMapping
    public List<Customer> getAllCustomers(HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/banking/customers - 모든 고객 조회 요청", httpRequest);
        try {
            log.info("모든 고객 목록 조회");
            return customerRepository.findAll();

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 특정 고객 조회
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id, HttpServletRequest httpRequest) {
        initializeContextAndLog("GET /api/banking/customers/" + id + " - 고객 조회 요청", httpRequest);
        try {
            Customer customer = customerRepository.findById(id);

            if (customer != null) {
                log.info("고객 조회 성공: {}", customer.getCustomerNumber());
                return ResponseEntity.ok(customer);
            } else {
                log.warn("고객을 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }

        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 고객 정보 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id,
                                       @Valid @RequestBody CustomerRequest request,
                                       BindingResult bindingResult,
                                       HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("PUT /api/banking/customers/" + id + " - 고객 수정 요청: " + request, httpRequest);
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage();
                log.warn("{}", errorMessage);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "guid", guid,
                        "message", errorMessage));
            }

            Customer customer = customerService.updateCustomer(id, request);
            log.info("고객 수정 성공: {}", customer.getCustomerNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guid", guid,
                    "message", "고객 정보가 성공적으로 수정되었습니다",
                    "customer", customer));

        } catch (Exception e) {
            log.error("고객 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "고객 수정 실패: " + e.getMessage()));
        } finally {
            ContextHolder.clearContext();
            MDC.remove("guid");
        }
    }

    // 고객 정지
    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspendCustomer(@PathVariable Long id, HttpServletRequest httpRequest) {
        String guid = initializeContextAndLog("POST /api/banking/customers/" + id + "/suspend - 고객 정지 요청", httpRequest);
        try {
            Customer customer = customerRepository.findById(id);
            if (customer == null) {
                log.warn("정지할 고객을 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            customer.changeStatus("SUSPENDED");
            customerRepository.update(customer);
            log.info("고객 정지 성공: {}", customer.getCustomerNumber());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", guid,
                "message", "고객이 정지되었습니다",
                "customer", customer
            ));
        } catch (Exception e) {
            log.error("고객 정지 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "guid", guid,
                "message", "고객 정지 실패: " + e.getMessage()
            ));
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