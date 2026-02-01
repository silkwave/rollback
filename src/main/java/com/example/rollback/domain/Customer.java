package com.example.rollback.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 고객 엔티티
@Slf4j
@Data
public class Customer {
    private Long id;
    private String customerNumber;
    private String name;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender; // M, F, OTHER
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String idNumber; // 주민등록번호 또는 사업자등록번호
    private String customerType; // INDIVIDUAL, BUSINESS
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String status; // ACTIVE, INACTIVE, SUSPENDED, CLOSED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // 개인 고객 생성
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

    // 법인 고객 생성
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

    // 고객 정보 업데이트
    public Customer updateInfo(String email, String phoneNumber, String address) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 정보 업데이트 - 고객번호: {}, 이메일: {}", customerNumber, email);
        return this;
    }

    // 고객 상태 변경
    public Customer changeStatus(String newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 상태 변경 - 고객번호: {}, 상태: {}", customerNumber, newStatus);
        return this;
    }

    // 마지막 로그인 시간 업데이트
    public Customer updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    // 리스크 레벨 변경
    public Customer updateRiskLevel(String newRiskLevel) {
        this.riskLevel = newRiskLevel;
        this.updatedAt = LocalDateTime.now();
        log.info("고객 리스크 레벨 변경 - 고객번호: {}, 리스크: {}", customerNumber, newRiskLevel);
        return this;
    }

    // 활성 고객 확인
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    // 개인 고객 확인
    public boolean isIndividual() {
        return "INDIVIDUAL".equals(this.customerType);
    }

    // 법인 고객 확인
    public boolean isBusiness() {
        return "BUSINESS".equals(this.customerType);
    }

    // 고객 유형 열거형
    public enum CustomerType {
        INDIVIDUAL, BUSINESS
    }

    // 고객 상태 열거형
    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED, CLOSED
    }

    // 리스크 레벨 열거형
    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
}