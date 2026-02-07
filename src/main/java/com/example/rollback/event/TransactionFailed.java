package com.example.rollback.event;

import com.example.rollback.util.ContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 거래 실패 이벤트
 * 
 * <p>트랜잭션이 롤백될 때 발생하는 Spring ApplicationEvent입니다.
 * 이벤트 기반 아키텍처의 핵심 요소로, 거래 실패 시 롤백이 완료된 후 
 * 관련 컴포넌트들에게 알림을 전달하는 역할을 합니다.</p>
 * 
 * <h3>주요 용도:</h3>
 * <ul>
 *   <li>거래 실패 알림 전송 (SMS, 이메일)</li>
 *   <li>실패 로그 기록</li>
 *   <li>모니터링 시스템에 실패 통보</li>
 *   <li>보안 감사 로그 작성</li>
 * </ul>
 * 
 * <p><strong>발생 타이밍:</strong><br>
 * {@code @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)} 
 * 어노테이션으로 리스너가 있어, 트랜잭션 롤백이 완전히 완료된 후에만 이벤트가 처리됩니다.</p>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Slf4j
@Data
public class TransactionFailed {
    
    /** 현재 요청의 고유 식별자 (요청 추적용) */
    private String guid;
    
    /** 실패 당시의 컨텍스트 정보 (요청 파라미터, 사용자 정보 등) */
    private Map<String, Object> context;
    
    /** 실패한 거래의 ID */
    private Long transactionId;
    
    /** 실패 상세 사유 */
    private String reason;

    /**
     * TransactionFailed 이벤트를 생성합니다.
     * 
     * @param context 실패 당시의 컨텍스트 정보
     * @param transactionId 실패한 거래의 ID
     * @param reason 실패 상세 사유
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
     * 현재 요청의 GUID를 반환합니다.
     * 
     * @return 요청 고유 식별자
     */
    public String getGuid() {
        return guid;
    }
    
    /**
     * 실패 당시의 컨텍스트 정보를 반환합니다.
     * 
     * @return 컨텍스트 맵
     */
    public Map<String, Object> getContext() {
        return context;
    }
    
    /**
     * 실패한 거래의 ID를 반환합니다.
     * 
     * @return 거래 ID
     */
    public Long getTransactionId() {
        return transactionId;
    }
    
    /**
     * 실패 사유를 반환합니다.
     * 
     * @return 실패 상세 사유
     */
    public String getReason() {
        return reason;
    }
}