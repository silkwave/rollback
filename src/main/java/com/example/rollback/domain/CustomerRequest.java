package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객 생성 요청 DTO 클래스
 * 
 * <p>새로운 고객을 생성하기 위해 필요한 정보를 담고 있는 데이터 전송 객체입니다.
 * Customer 엔티티로 변환하는 역할을 합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>고객 정보 유효성 검사</li>
 *   <li>Customer 엔티티 변환</li>
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
    
    /**
     * CustomerRequest를 Customer 엔티티로 변환합니다
     * 
     * @param customerNumber 생성될 고객번호
     * @return 변환된 Customer 엔티티
     */
    public Customer toCustomer(String customerNumber) {
        log.debug("CustomerRequest to Customer - name: {}, email: {}", name, email);
        
        return Customer.create(customerNumber, name, email, phoneNumber);
    }
}