package com.example.rollback.repository;

import com.example.rollback.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 거래 리포지토리 인터페이스
 * 
 * <p>MyBatis를 사용하여 거래 데이터에 대한 CRUD 작업을 수행합니다.
 * 데이터베이스의 transactions 테이블과 매핑되며, 거래 관련 모든 데이터 접근 로직을 제공합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>거래 생성, 조회, 수정, 삭제</li>
 *   <li>GUID 기반 거래 검색 (요청 추적용)</li>
 *   <li>계좌 또는 고객별 거래 내역 조회</li>
 *   <li>거래 상태 업데이트</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Mapper
public interface TransactionRepository {
    
    /**
     * 새로운 거래를 저장합니다.
     * 
     * @param transaction 저장할 거래 정보
     */
    void save(Transaction transaction);
    
    /**
     * ID로 거래를 조회합니다.
     * 
     * @param id 조회할 거래의 ID
     * @return 거래 정보 (존재하지 않는 경우 null)
     */
    Transaction findById(@Param("id") Long id);
    
    /**
     * GUID로 거래를 조회합니다.
     * 
     * @param guid 조회할 거래의 GUID (요청 추적용)
     * @return 거래 정보 (존재하지 않는 경우 null)
     */
    Transaction findByGuid(@Param("guid") String guid);
    
    /**
     * 특정 계좌의 모든 거래를 조회합니다.
     * 
     * @param accountId 계좌 ID
     * @return 해당 계좌의 거래 목록
     */
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
    
    /**
     * 특정 고객의 모든 거래를 조회합니다.
     * 
     * @param customerId 고객 ID
     * @return 해당 고객의 거래 목록
     */
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 모든 거래 목록을 조회합니다.
     * 
     * @return 전체 거래 목록
     */
    List<Transaction> findAll();
    
    /**
     * 거래 상태를 업데이트합니다.
     * 
     * @param id 업데이트할 거래의 ID
     * @param status 새로운 상태 (PENDING, COMPLETED, FAILED 등)
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 거래 정보 전체를 업데이트합니다.
     * 
     * @param transaction 업데이트할 거래 정보
     */
    void update(Transaction transaction);
    
    /**
     * 거래를 삭제합니다.
     * 
     * @param id 삭제할 거래의 ID
     */
    void delete(@Param("id") Long id);
}