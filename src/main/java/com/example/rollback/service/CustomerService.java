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
 
/**
 * 고객 관리 서비스
 * 
 * <p>고객 생성, 정보 수정, 조회 등 고객과 관련된 모든 비즈니스 로직을 처리합니다.
 * 개인 고객과 법인 고객을 모두 지원하며, 위험 등급 평가와 상태 관리 기능이 포함됩니다.
 * 모든 작업은 트랜잭션 내에서 처리되어 데이터 일관성을 보장합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>신규 고객 등록 (개인/법인)</li>
 *   <li>고객 정보 수정 및 조회</li>
 *   <li>고객 상태 관리</li>
 *   <li>위험 등급 평가</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    /** 고객 리포지토리 - 고객 데이터 접근 */
    private final CustomerRepository customerRepository;

    /**
     * 새로운 고객을 생성합니다.
     * 
     * <p>고객번호를 자동으로 생성하고 고객 정보를 데이터베이스에 저장합니다.
     * 개인 고객과 법인 고객을 모두 지원합니다.</p>
     * 
     * @param request 고객 생성 요청 정보
     * @return 생성된 고객 정보
     * @throws RuntimeException 고객 생성 실패 시 발생
     */
    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        return measureExecutionTime(() -> {
            log.info("고객 생성 시작 - 이름: {}", request.getName());

            String customerNumber = IdGenerator.generate("CUST");
            log.info("고객번호 생성: {}", customerNumber);

            Customer customer = request.toCustomer(customerNumber);
            customerRepository.save(customer);
            log.info("고객 저장 완료 - ID: {}", customer.getId());

            return customer;
        }, "고객 생성");
    }

    /**
     * ID로 고객을 조회합니다.
     * 
     * @param id 조회할 고객의 ID
     * @return 고객 정보 (존재하지 않는 경우 null)
     */
    public Customer findById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * 모든 고객 목록을 조회합니다.
     * 
     * @return 전체 고객 목록
     */
    public java.util.List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * 고객번호로 고객을 조회합니다.
     * 
     * @param customerNumber 조회할 고객번호
     * @return 고객 정보 (존재하지 않는 경우 null)
     */
    public Customer findByCustomerNumber(String customerNumber) {
        return customerRepository.findByCustomerNumber(customerNumber);
    }

    /**
     * 고객 정보을 업데이트합니다.
     * 
     * <p>기존 고객의 정보를 요청된 정보로 수정합니다.
     * 이름, 연락처, 주소 등 개인 정보를 업데이트할 수 있습니다.</p>
     * 
     * @param id 업데이트할 고객의 ID
     * @param request 수정할 고객 정보
     * @return 수정된 고객 정보
     * @throws RuntimeException 고객 정보 수정 실패 시 발생
     */
    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {
        final String guid = ContextHolder.getCurrentGuid();
        MDC.put("guid", guid);

        return measureExecutionTime(() -> {
            log.info("고객 정보 업데이트 시작 - ID: {}", id);

            Customer existingCustomer = customerRepository.findById(id);
            if (existingCustomer == null) {
                throw new IllegalArgumentException("고객을 찾을 수 없습니다: " + id);
            }

            // 정보 업데이트
            existingCustomer.updateInfo(request.getEmail(), request.getPhoneNumber()); // Removed address
            customerRepository.update(existingCustomer);
            log.info("고객 정보 업데이트 완료 - ID: {}", id);

            return existingCustomer;
        }, "고객 정보 업데이트");
    }

    /**
     * 주어진 작업을 실행하고 실행 시간을 측정하여 로깅합니다.
     * @param <T> 작업의 반환 타입
     * @param task 실행할 작업 (Supplier 형태)
     * @param taskName 로깅에 사용될 작업의 이름
     * @return 작업의 결과
     * @throws RuntimeException 작업 실행 중 발생한 예외
     */
    private <T> T measureExecutionTime(java.util.function.Supplier<T> task, String taskName) {
        long startTime = System.currentTimeMillis();
        try {
            T result = task.get();
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} 완료 - 소요시간: {}ms", taskName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} 실패 - 소요시간: {}ms", taskName, duration, ex);
            throw new RuntimeException(ex);
        }
    }

}