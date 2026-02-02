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
 *   <li>개인/법인 고객 생성</li>
 *   <li>고객 정보 관리</li>
 *   <li>고객 상태 및 리스크 레벨 관리</li>
 *   <li>로그인 기록 관리</li>
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
    
    /** 생년월일 (개인 고객의 경우) */
    private String dateOfBirth;
    
    /** 성별 (M: 남성, F: 여성, OTHER: 기타) */
    private String gender;
    
    /** 주소 */
    private String address;
    
    /** 도시 */
    private String city;
    
    /** 국가 */
    private String country;
    
    /** 우편번호 */
    private String postalCode;
    
    /** 신분번호 (개인: 주민등록번호, 법인: 사업자등록번호) */
    private String idNumber;
    
    /** 고객 유형 (INDIVIDUAL: 개인, BUSINESS: 법인) */
    private String customerType;
    
    /** 리스크 레벨 (LOW: 저위험, MEDIUM: 중위험, HIGH: 고위험) */
    private String riskLevel;
    
    /** 고객 상태 (ACTIVE: 활성, INACTIVE: 비활성, SUSPENDED: 정지, CLOSED: 폐쇄) */
    private String status;
    
    /** 고객 정보 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 고객 정보 최종 수정 일시 */
    private LocalDateTime updatedAt;
    
    /** 마지막 로그인 일시 */
    private LocalDateTime lastLoginAt;

    /**
     * 개인 고객을 생성하는 팩토리 메서드
     * 
     * @param customerNumber 고객번호 (중복되지 않아야 함)
     * @param name 고객 성명
     * @param email 이메일 주소
     * @param phoneNumber 전화번호
     * @param dateOfBirth 생년월일
     * @param idNumber 주민등록번호
     * @return 생성된 Customer 객체
     */
    public static Customer createIndividual(String customerNumber, String name, String email,
                                          String phoneNumber, String dateOfBirth, String idNumber) {
        Customer customer = new Customer();
        customer.customerNumber = customerNumber;
        customer.name = name;
        customer.email = email;
        customer.phoneNumber = phoneNumber;
        customer.dateOfBirth = dateOfBirth;
        customer.idNumber = idNumber;
        customer.customerType = "INDIVIDUAL";
        customer.riskLevel = "LOW";
        customer.status = "ACTIVE";
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();
        
        log.info("개인 고객 생성 - 고객번호: {}, 이름: {}, 이메일: {}", customerNumber, name, email);
        return customer;
    }

    /**
     * 법인 고객을 생성하는 팩토리 메서드
     * 
     * @param customerNumber 고객번호 (중복되지 않아야 함)
     * @param name 법인명
     * @param email 대표 이메일 주소
     * @param phoneNumber 대표 전화번호
     * @param idNumber 사업자등록번호
     * @return 생성된 Customer 객체
     */
    public static Customer createBusiness(String customerNumber, String name, String email,
                                        String phoneNumber, String idNumber) {
        Customer customer = new Customer();
        customer.customerNumber = customerNumber;
        customer.name = name;
        customer.email = email;
        customer.phoneNumber = phoneNumber;
        customer.idNumber = idNumber;
        customer.customerType = "BUSINESS";
        customer.riskLevel = "MEDIUM";
        customer.status = "ACTIVE";
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();
        
        log.info("법인 고객 생성 - 고객번호: {}, 이름: {}, 사업자번호: {}", customerNumber, name, idNumber);
        return customer;
    }

    /**
     * 고객의 기본 정보를 업데이트합니다
     * 
     * @param email 새로운 이메일 주소
     * @param phoneNumber 새로운 전화번호
     * @param address 새로운 주소
     * @return 정보가 업데이트된 Customer 객체
     */
    public Customer updateInfo(String email, String phoneNumber, String address) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
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
    public Customer changeStatus(String newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 상태 변경 - 고객번호: {}, 상태: {}", customerNumber, newStatus);
        return this;
    }

    /**
     * 고객의 마지막 로그인 시간을 현재 시간으로 업데이트합니다
     * 
     * @return 로그인 시간이 업데이트된 Customer 객체
     */
    public Customer updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * 고객의 리스크 레벨을 변경합니다
     * 
     * <p>고객의 거래 패턴, 신용도 등을 평가하여 리스크 레벨을 조정합니다.</p>
     * 
     * @param newRiskLevel 새로운 리스크 레벨 (LOW, MEDIUM, HIGH)
     * @return 리스크 레벨이 변경된 Customer 객체
     */
    public Customer updateRiskLevel(String newRiskLevel) {
        this.riskLevel = newRiskLevel;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 리스크 레벨 변경 - 고객번호: {}, 리스크: {}", customerNumber, newRiskLevel);
        return this;
    }

    /**
     * 고객이 활성 상태인지 확인합니다
     * 
     * @return 활성 상태이면 true, 아니면 false
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    /**
     * 개인 고객인지 확인합니다
     * 
     * @return 개인 고객이면 true, 아니면 false
     */
    public boolean isIndividual() {
        return "INDIVIDUAL".equals(this.customerType);
    }

    /**
     * 법인 고객인지 확인합니다
     * 
     * @return 법인 고객이면 true, 아니면 false
     */
    public boolean isBusiness() {
        return "BUSINESS".equals(this.customerType);
    }

    /**
     * 고객 유형 열거형
     */
    public enum CustomerType {
        /** 개인 고객 */
        INDIVIDUAL, 
        /** 법인 고객 */
        BUSINESS
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

    /**
     * 리스크 레벨 열거형
     */
    public enum RiskLevel {
        /** 저위험 - 정상적인 거래 패턴 */
        LOW, 
        /** 중위험 - 일부 거래 모니터링 필요 */
        MEDIUM, 
        /** 고위험 - 심층 모니터링 및 제한 필요 */
        HIGH
    }
}