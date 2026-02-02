package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 생성 요청 DTO 클래스
 * 
 * <p>새로운 고객을 생성하기 위해 필요한 정보를 담고 있는 데이터 전송 객체입니다.
 * 개인 고객과 법인 고객 생성을 모두 지원하며, Customer 엔티티로 변환하는 역할을 합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>고객 정보 유효성 검사</li>
 *   <li>Customer 엔티티 변환</li>
 *   <li>개인/법인 고객 구분 생성</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024-01-01
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
    
    /** 고객 유형 (필수) - INDIVIDUAL, BUSINESS */
    @NotBlank(message = "고객 유형은 필수입니다")
    private String customerType;
    
    /** 생년월일 (개인 고객의 경우 선택사항) */
    private String dateOfBirth;
    
    /** 신분번호 (개인: 주민등록번호, 법인: 사업자등록번호) */
    private String idNumber;
    
    /** 주소 (선택사항) */
    private String address;
    
    /** 도시 (선택사항) */
    private String city;
    
    /** 국가 (선택사항) */
    private String country;
    
    /** 우편번호 (선택사항) */
    private String postalCode;

    /**
     * CustomerRequest를 Customer 엔티티로 변환합니다
     * 
     * <p>고객 유형에 따라 개인 고객 또는 법인 고객을 생성합니다.</p>
     * 
     * @param customerNumber 생성될 고객번호
     * @return 변환된 Customer 엔티티
     */
    public Customer toCustomer(String customerNumber) {
        log.debug("CustomerRequest to Customer - name: {}, email: {}, type: {}", name, email, customerType);
        
        if ("INDIVIDUAL".equals(customerType)) {
            return Customer.createIndividual(customerNumber, name, email, phoneNumber, dateOfBirth, idNumber);
        } else {
            return Customer.createBusiness(customerNumber, name, email, phoneNumber, idNumber);
        }
    }
}