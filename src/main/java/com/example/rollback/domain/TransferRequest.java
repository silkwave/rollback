package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

// 이체 요청 DTO
@Slf4j
@Data
public class TransferRequest {
    
    @NotNull(message = "출금 계좌 ID는 필수입니다")
    private Long fromAccountId;
    
    @NotNull(message = "입금 계좌 ID는 필수입니다")
    private Long toAccountId;
    
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
        log.debug("TransferRequest to Transaction - fromAccountId: {}, toAccountId: {}, amount: {}", 
            fromAccountId, toAccountId, amount);
        
        return Transaction.createTransfer(guid, fromAccountId, toAccountId, customerId, amount, currency, description);
    }
}