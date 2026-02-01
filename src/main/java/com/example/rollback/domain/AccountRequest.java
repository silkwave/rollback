package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

// 계좌 개설 요청 DTO
@Data
@Slf4j
public class AccountRequest {
    
    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;
    
    @NotBlank(message = "계좌 유형은 필수입니다")
    private String accountType; // CHECKING, SAVINGS, CREDIT
    
    @NotBlank(message = "통화는 필수입니다")
    private String currency; // KRW, USD, EUR
    
    @NotBlank(message = "계좌주 명은 필수입니다")
    private String accountHolderName;
    
    private String branchCode;
    
    private BigDecimal initialDeposit;
    
    private boolean forceFailure = false;

    // Account 엔티티 변환
    public Account toAccount(String accountNumber) {
        log.debug("AccountRequest to Account - customerId: {}, accountType: {}, currency: {}, initialDeposit: {}", 
            customerId, accountType, currency, initialDeposit);
        
        AccountType accType = AccountType.valueOf(accountType.toUpperCase());
        Account account = Account.create(accountNumber, customerId, accType, currency, initialDeposit, accountHolderName);
        
        // Set optional branch code if provided
        if (branchCode != null && !branchCode.trim().isEmpty()) {
            account.setBranchCode(branchCode);
        }
        
        return account;
    }
    
    // Additional getter for account number (would be set by service)
    public String getAccountNumber() {
        return null;
    }
}