package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

// 입금 요청 DTO
@Slf4j
@Data
public class DepositRequest {
    
    @NotNull(message = "계좌 ID는 필수입니다")
    private Long accountId;
    
    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;
    
    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 0보다 커야 합니다")
    private BigDecimal amount;
    
    @NotBlank(message = "통화는 필수입니다")
    private String currency;
    
    private String description;
    
    private boolean forceFailure = false;

    // Transaction 엔티티 변환
    public Transaction toTransaction(String guid) {
        log.debug("DepositRequest to Transaction - accountId: {}, amount: {}, currency: {}", 
            accountId, amount, currency);
        
        return Transaction.createDeposit(guid, accountId, customerId, amount, currency, description);
    }
}