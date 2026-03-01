package com.example.rollback.event;

import com.example.rollback.util.ContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 거래 실패(롤백) 이벤트입니다.
 */
@Slf4j
@Data
public class TransactionFailed {
    
    /** 요청 GUID */
    private String guid;
    
    /** 실패 당시 컨텍스트 */
    private Map<String, Object> context;
    
    /** 거래 ID */
    private Long transactionId;
    
    /** 실패 사유 */
    private String reason;

    /**
     * 이벤트를 생성합니다.
     */
    public TransactionFailed(Map<String, Object> context, Long transactionId, String reason) {
        this.guid = ContextHolder.getCurrentGuid();
        this.context = context;
        this.transactionId = transactionId;
        this.reason = reason;
        
        log.info("");
        log.info("");
        log.info("");
        log.info("");
        log.info("");
        log.info("==============================================================");        
        log.info("TransactionFailed 이벤트 생성 - GUID: {}, 거래ID: {}, 사유: {}", guid, transactionId, reason);
    }
    
    /**
     * 요청 GUID를 반환합니다.
     */
    public String getGuid() {
        return guid;
    }
    
    /**
     * 컨텍스트를 반환합니다.
     */
    public Map<String, Object> getContext() {
        return context;
    }
    
    /**
     * 거래 ID를 반환합니다.
     */
    public Long getTransactionId() {
        return transactionId;
    }
    
    /**
     * 실패 사유를 반환합니다.
     */
    public String getReason() {
        return reason;
    }
}
