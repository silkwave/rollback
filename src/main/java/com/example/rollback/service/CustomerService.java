package com.example.rollback.service;

import com.example.rollback.domain.Customer;
import com.example.rollback.domain.CustomerRequest;
import com.example.rollback.repository.CustomerRepository;
import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 고객 관리 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    // 고객 생성
    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        try {
            log.info("고객 생성 시작 - 이름: {}, 유형: {}", request.getName(), request.getCustomerType());

            // 1. 고객번호 생성
            String customerNumber = IdGenerator.generate("CUST");
            log.info("고객번호 생성: {}", customerNumber);

            // 2. 고객 생성
            Customer customer = request.toCustomer(customerNumber);
            customerRepository.save(customer);
            log.info("고객 저장 완료 - ID: {}", customer.getId());

            long duration = System.currentTimeMillis() - startTime;
            log.info("고객 생성 완료 - 고객번호: {}, 소요시간: {}ms", customerNumber, duration);
            return customer;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("고객 생성 실패 - 소요시간: {}ms, 사유: {}", duration, e.getMessage(), e);
            throw new RuntimeException("고객 생성에 실패했습니다.", e);
        }
    }

    // 고객 조회
    public Customer findById(Long id) {
        return customerRepository.findById(id);
    }

    // 전체 고객 조회
    public java.util.List<Customer> findAll() {
        return customerRepository.findAll();
    }

    // 고객번호로 조회
    public Customer findByCustomerNumber(String customerNumber) {
        return customerRepository.findByCustomerNumber(customerNumber);
    }

    // 고객 정보 업데이트
    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {
        long startTime = System.currentTimeMillis();
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        try {
            log.info("고객 정보 업데이트 시작 - ID: {}", id);

            Customer existingCustomer = customerRepository.findById(id);
            if (existingCustomer == null) {
                throw new IllegalArgumentException("고객을 찾을 수 없습니다: " + id);
            }

            // 정보 업데이트
            existingCustomer.updateInfo(request.getEmail(), request.getPhoneNumber(), request.getAddress());
            customerRepository.update(existingCustomer);
            log.info("고객 정보 업데이트 완료 - ID: {}", id);

            long duration = System.currentTimeMillis() - startTime;
            log.info("고객 정보 업데이트 성공 - 소요시간: {}ms", duration);
            return existingCustomer;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("고객 정보 업데이트 실패 - 소요시간: {}ms, 사유: {}", duration, e.getMessage(), e);
            throw new RuntimeException("고객 정보 업데이트에 실패했습니다.", e);
        }
    }

}