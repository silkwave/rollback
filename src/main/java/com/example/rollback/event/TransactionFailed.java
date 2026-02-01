package com.example.rollback.event;

import com.example.rollback.util.ContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

// 거래 실패 이벤트
@Slf4j
@Data
public class TransactionFailed {
    private String guid;
    private Map<String, Object> context;
    private Long transactionId;
    private String reason;

    public TransactionFailed(Map<String, Object> context, Long transactionId, String reason) {
        this.guid = ContextHolder.getCurrentGuid();
        this.context = context;
        this.transactionId = transactionId;
        this.reason = reason;
        
        log.info("TransactionFailed 이벤트 생성 - GUID: {}, 거래ID: {}, 사유: {}", guid, transactionId, reason);
    }
    
    // Additional getters
    public String getGuid() {
        return guid;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public Long getTransactionId() {
        return transactionId;
    }
    
    public String getReason() {
        return reason;
    }
}