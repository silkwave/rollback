package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 고객 엔티티 클래스
 * 
 * <p>이 클래스는 은행 고객의 모든 정보를 관리하며, 개인 고객과 법인 고객의
 * 정보를 통합적으로 처리합니다. 고객 생성, 정보 업데이트, 상태 관리 등의 기능을 제공합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>개인/법인 고객 생성 (간소화)</li>
 *   <li>고객 정보 관리</li>
 *   <li>고객 상태 관리</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Data
public class Customer {
    /** 고객 고유 ID */
    private Long id;
    
    /** 고객번호 (중복되지 않는 고유 값) */
    private String customerNumber;
    
    /** 고객 성명 */
    private String name;
    
    /** 이메일 주소 */
    private String email;
    
    /** 전화번호 */
    private String phoneNumber;
    
    /** 고객 상태 (ACTIVE: 활성, INACTIVE: 비활성, SUSPENDED: 정지, CLOSED: 폐쇄) */
    private CustomerStatus status; // Changed to enum type
    
    /** 고객 정보 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 고객 정보 최종 수정 일시 */
    private LocalDateTime updatedAt;

    /**
     * 고객을 생성하는 팩토리 메서드 (개인/법인 통합)
     * 
     * @param customerNumber 고객번호 (중복되지 않아야 함)
     * @param name 고객 성명
     * @param email 이메일 주소
     * @param phoneNumber 전화번호
     * @return 생성된 Customer 객체
     */
    public static Customer create(String customerNumber, String name, String email, String phoneNumber) {
        Customer customer = new Customer();
        customer.customerNumber = customerNumber;
        customer.name = name;
        customer.email = email;
        customer.phoneNumber = phoneNumber;
        customer.status = CustomerStatus.ACTIVE; // Default to enum
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();
        
        log.info("고객 생성 - 고객번호: {}, 이름: {}, 이메일: {}", customerNumber, name, email);
        return customer;
    }

    /**
     * 고객의 기본 정보를 업데이트합니다
     * 
     * @param email 새로운 이메일 주소
     * @param phoneNumber 새로운 전화번호
     * @return 정보가 업데이트된 Customer 객체
     */
    public Customer updateInfo(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 정보 업데이트 - 고객번호: {}, 이메일: {}", customerNumber, email);
        return this;
    }

    /**
     * 고객의 상태를 변경합니다
     * 
     * @param newStatus 새로운 상태 (ACTIVE, INACTIVE, SUSPENDED, CLOSED)
     * @return 상태가 변경된 Customer 객체
     */
    public Customer changeStatus(CustomerStatus newStatus) { // Changed parameter type
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 상태 변경 - 고객번호: {}, 상태: {}", customerNumber, newStatus);
        return this;
    }

    /**
     * 고객이 활성 상태인지 확인합니다
     * 
     * @return 활성 상태이면 true, 아니면 false
     */
    public boolean isActive() {
        return CustomerStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * 고객 상태 열거형
     */
    public enum CustomerStatus {
        /** 활성 상태 - 모든 서비스 이용 가능 */
        ACTIVE, 
        /** 비활성 상태 - 일시적으로 서비스 제한 */
        INACTIVE, 
        /** 정지 상태 - 관리자 조치 필요 */
        SUSPENDED, 
        /** 폐쇄 상태 - 계약 종료 */
        CLOSED
    }
}