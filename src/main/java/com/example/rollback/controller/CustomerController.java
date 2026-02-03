package com.example.rollback.controller;

import com.example.rollback.domain.Customer;
import com.example.rollback.domain.CustomerRequest;
import com.example.rollback.service.CustomerService;
import com.example.rollback.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 고객 관련 REST API 컨트롤러
 * 
 * <p>고객 생성, 정보 수정, 상태 관리(정지) 등 고객과 관련된 모든 REST API 엔드포인트를 제공합니다.
 * 개인 고객과 법인 고객을 모두 지원하며, 위험 등급 평가와 상태 관리 기능이 포함됩니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>신규 고객 등록 (개인/법인)</li>
 *   <li>고객 정보 조회 및 수정</li>
 *   <li>고객 상태 관리 (활성/정지/해지)</li>
 *   <li>고객 목록 조회</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking/customers")
public class CustomerController {

    /** 고객 서비스 - 고객 관련 비즈니스 로직 처리 */
    private final CustomerService customerService;
    
    /** 고객 리포지토리 - 고객 데이터 접근 */
    private final CustomerRepository customerRepository;

    /**
     * 새로운 고객을 생성하는 엔드포인트
     * 
     * @param request 고객 생성 요청 정보 (이름, 유형, 연락처 등)
     * @return 생성된 고객 정보와 처리 결과
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        log.info("고객 생성 성공: {}", customer.getCustomerNumber());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", MDC.get("guid"),
                "message", "고객이 성공적으로 생성되었습니다",
                "customer", customer));
    }

    /**
     * 모든 고객 목록을 조회하는 엔드포인트
     * 
     * @return 전체 고객 목록
     */
    @GetMapping
    public List<Customer> getAllCustomers() {
        log.info("모든 고객 목록 조회");
        return customerRepository.findAll();
    }

    /**
     * 특정 ID의 고객을 조회하는 엔드포인트
     * 
     * @param id 조회할 고객의 ID
     * @return 고객 정보 (존재하지 않는 경우 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id);

        if (customer != null) {
            log.info("고객 조회 성공: {}", customer.getCustomerNumber());
            return ResponseEntity.ok(customer);
        } else {
            log.warn("고객을 찾을 수 없음: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 기존 고객 정보를 수정하는 엔드포인트
     * 
     * @param id 수정할 고객의 ID
     * @param request 수정할 고객 정보
     * @return 수정된 고객 정보와 처리 결과
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Customer customer = customerService.updateCustomer(id, request);
        log.info("고객 수정 성공: {}", customer.getCustomerNumber());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "guid", MDC.get("guid"),
                "message", "고객 정보가 성공적으로 수정되었습니다",
                "customer", customer));
    }

    /**
     * 고객을 정지 상태로 변경하는 엔드포인트
     * 정지된 고객은 계좌 개설이나 거래가 제한됩니다.
     * 
     * @param id 정지할 고객의 ID
     * @return 처리 결과와 업데이트된 고객 정보
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspendCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id);
        if (customer == null) {
            log.warn("정지할 고객을 찾을 수 없음: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        customer.changeStatus(Customer.CustomerStatus.SUSPENDED);
        customerRepository.update(customer);
        log.info("고객 정지 성공: {}", customer.getCustomerNumber());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "guid", MDC.get("guid"),
            "message", "고객이 정지되었습니다",
            "customer", customer
        ));
    }
}