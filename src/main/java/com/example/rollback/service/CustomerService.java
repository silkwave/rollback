package com.example.rollback.service;

import com.example.rollback.aop.MeasuredExecutionTime;
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
 
/**
 * 고객 생성/수정을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    /** 고객 저장소 */
    private final CustomerRepository customerRepository;

    /**
     * 고객을 생성합니다.
     */
    @Transactional
    @MeasuredExecutionTime("고객 생성")
    public Customer createCustomer(CustomerRequest request) {
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        log.info("고객 생성 시작 - 이름: {}", request.getName());

        String customerNumber = IdGenerator.generate("CUST");
        log.info("고객번호 생성: {}", customerNumber);

        Customer customer = request.toCustomer(customerNumber);
        customerRepository.save(customer);
        log.info("고객 저장 완료 - ID: {}", customer.getId());

        return customer;
    }

    /**
     * 고객 단건을 조회합니다.
     */
    public Customer findById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * 고객 목록을 조회합니다.
     */
    public java.util.List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * 고객번호로 고객을 조회합니다.
     */
    public Customer findByCustomerNumber(String customerNumber) {
        return customerRepository.findByCustomerNumber(customerNumber);
    }

    /**
     * 고객 정보를 수정합니다.
     */
    @Transactional
    @MeasuredExecutionTime("고객 정보 업데이트")
    public Customer updateCustomer(Long id, CustomerRequest request) {
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        log.info("고객 정보 업데이트 시작 - ID: {}", id);

        Customer existingCustomer = customerRepository.findById(id);
        if (existingCustomer == null) {
            throw new IllegalArgumentException("고객을 찾을 수 없습니다: " + id);
        }

        // 연락처 갱신
        existingCustomer.updateInfo(request.getEmail(), request.getPhoneNumber());
        customerRepository.update(existingCustomer);
        log.info("고객 정보 업데이트 완료 - ID: {}", id);

        return existingCustomer;
    }

}
