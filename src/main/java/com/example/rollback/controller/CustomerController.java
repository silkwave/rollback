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
 * 고객 API를 제공합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banking/customers")
public class CustomerController {

    /** 고객 서비스 */
    private final CustomerService customerService;
    
    /** 고객 조회/수정용 리포지토리 */
    private final CustomerRepository customerRepository;

    /**
     * 고객을 생성합니다.
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
     * 고객 목록을 조회합니다.
     */
    @GetMapping
    public List<Customer> getAllCustomers() {
        log.info("모든 고객 목록 조회");
        return customerRepository.findAll();
    }

    /**
     * 고객 단건을 조회합니다.
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
     * 고객 정보를 수정합니다.
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
     * 고객을 정지합니다.
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
