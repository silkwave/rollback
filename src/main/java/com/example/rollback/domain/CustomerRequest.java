package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 생성 요청 DTO입니다.
 */
@Slf4j
@Data
public class CustomerRequest {
    
    /** 고객 성명 (필수) */
    @NotBlank(message = "고객 이름은 필수입니다")
    private String name;
    
    /** 이메일 주소 (필수) */
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
    
    /** 전화번호 (필수) */
    @NotBlank(message = "전화번호는 필수입니다")
    private String phoneNumber;
    
    /**
     * Customer로 변환합니다.
     */
    public Customer toCustomer(String customerNumber) {
        log.debug("CustomerRequest to Customer - name: {}, email: {}", name, email);
        
        return Customer.create(customerNumber, name, email, phoneNumber);
    }
}
