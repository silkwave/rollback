package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// 고객 생성 요청 DTO
@Slf4j
@Data
public class CustomerRequest {
    
    @NotBlank(message = "고객 이름은 필수입니다")
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
    
    @NotBlank(message = "전화번호는 필수입니다")
    private String phoneNumber;
    
    @NotBlank(message = "고객 유형은 필수입니다")
    private String customerType; // INDIVIDUAL, BUSINESS
    
    private String dateOfBirth; // 개인 고객의 경우
    
    private String idNumber; // 주민등록번호 또는 사업자등록번호
    
    private String address;
    
    private String city;
    
    private String country;
    
    private String postalCode;

    // Customer 엔티티 변환
    public Customer toCustomer(String customerNumber) {
        log.debug("CustomerRequest to Customer - name: {}, email: {}, type: {}", name, email, customerType);
        
        if ("INDIVIDUAL".equals(customerType)) {
            return Customer.createIndividual(customerNumber, name, email, phoneNumber, dateOfBirth, idNumber);
        } else {
            return Customer.createBusiness(customerNumber, name, email, phoneNumber, idNumber);
        }
    }
}