package com.example.rollback.repository;

import com.example.rollback.domain.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 알림 로그 리포지토리 인터페이스
 * 
 * <p>MyBatis를 사용하여 알림 로그 데이터에 대한 CRUD 작업을 수행합니다.
 * 데이터베이스의 notification_logs 테이블과 매핑되며, 알림 관련 모든 데이터 접근 로직을 제공합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>알림 로그 저장 및 조회</li>
 *   <li>계좌, 고객, 거래별 알림 내역 조회</li>
 *   <li>전체 알림 로그 목록 조회</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 * 알림 로그 데이터베이스 작업을 위한 MyBatis Mapper 인터페이스.
 */
@Mapper
public interface NotificationLogRepository {
    
    /**
     * 새로운 알림 로그를 저장합니다.
     * 
     * @param log 저장할 알림 로그 정보
     */
    void save(NotificationLog log);
    
    /**
     * ID로 알림 로그를 조회합니다.
     * 
     * @param id 조회할 알림 로그의 ID
     * @return 알림 로그 정보 (존재하지 않는 경우 null)
     */
    NotificationLog findById(@Param("id") Long id);
    
    /**
     * 모든 알림 로그 목록을 조회합니다.
     * 
     * @return 전체 알림 로그 목록
     */
    List<NotificationLog> findAll();
    
    /**
     * 특정 계좌와 관련된 알림 로그를 조회합니다.
     * 
     * @param accountId 계좌 ID
     * @return 해당 계좌의 알림 로그 목록
     */
    List<NotificationLog> findByAccountId(@Param("accountId") Long accountId);
    
    /**
     * 특정 거래와 관련된 알림 로그를 조회합니다.
     * 
     * @param transactionId 거래 ID
     * @return 해당 거래의 알림 로그 목록
     */
    List<NotificationLog> findByTransactionId(@Param("transactionId") Long transactionId);
    
    /**
     * 특정 고객과 관련된 알림 로그를 조회합니다.
     * 
     * @param customerId 고객 ID
     * @return 해당 고객의 알림 로그 목록
     */
    List<NotificationLog> findByCustomerId(@Param("customerId") Long customerId);


}