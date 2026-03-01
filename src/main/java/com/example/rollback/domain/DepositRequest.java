package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 입금 요청 DTO입니다.
 */
@Slf4j
@Data
public class DepositRequest {
    
    /** 입금받을 계좌 ID (필수) */
    @NotNull(message = "계좌 ID는 필수입니다")
    private Long accountId;
    
    /** 거래를 요청한 고객 ID (필수) */
    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;
    
    /** 입금 금액 (필수, 0보다 커야 함) */
    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 0보다 커야 합니다")
    private BigDecimal amount;
    
    /** 통화 코드 (필수) */
    @NotBlank(message = "통화는 필수입니다")
    private String currency;
    
    /** 거래 설명 또는 메모 (선택사항) */
    private String description;
    
    /** 테스트용 실패 강제 여부 */
    private boolean forceFailure = false;

    /**
     * 입금 거래(Transaction)로 변환합니다.
     */
    public Transaction toTransaction(String guid) {
        log.debug("DepositRequest to Transaction - accountId: {}, amount: {}, currency: {}", 
            accountId, amount, currency);
        
        return Transaction.createDeposit(guid, accountId, customerId, amount, currency, description);
    }
}
