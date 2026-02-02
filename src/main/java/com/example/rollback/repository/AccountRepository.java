package com.example.rollback.repository;

import com.example.rollback.domain.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 계좌 리포지토리 인터페이스
 * 
 * <p>MyBatis를 사용하여 계좌 데이터에 대한 CRUD 작업을 수행합니다.
 * 데이터베이스의 accounts 테이블과 매핑되며, 계좌 관련 모든 데이터 접근 로직을 제공합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>계좌 생성, 조회, 수정, 삭제</li>
 *   <li>계좌번호 또는 고객ID 기반 검색</li>
 *   <li>잔액 및 상태 업데이트</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Mapper
public interface AccountRepository {
    
    /**
     * 새로운 계좌를 저장합니다.
     * 
     * @param account 저장할 계좌 정보
     */
    void save(Account account);
    
    /**
     * ID로 계좌를 조회합니다.
     * 
     * @param id 조회할 계좌의 ID
     * @return 계좌 정보 (존재하지 않는 경우 null)
     */
    Account findById(@Param("id") Long id);
    
    /**
     * 계좌번호로 계좌를 조회합니다.
     * 
     * @param accountNumber 조회할 계좌번호
     * @return 계좌 정보 (존재하지 않는 경우 null)
     */
    Account findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * 특정 고객의 모든 계좌를 조회합니다.
     * 
     * @param customerId 고객 ID
     * @return 해당 고객의 계좌 목록
     */
    List<Account> findByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 모든 계좌 목록을 조회합니다.
     * 
     * @return 전체 계좌 목록
     */
    List<Account> findAll();
    
    /**
     * 계좌 잔액을 업데이트합니다.
     * 
     * @param account 잔액이 변경된 계좌 정보
     */
    void updateBalance(Account account);
    
    /**
     * 계좌 상태를 업데이트합니다.
     * 
     * @param id 업데이트할 계좌의 ID
     * @param status 새로운 상태 (ACTIVE, FROZEN, CLOSED 등)
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 계좌 정보 전체를 업데이트합니다.
     * 
     * @param account 업데이트할 계좌 정보
     */
    void update(Account account);
    
    /**
     * 계좌를 삭제합니다.
     * 
     * @param id 삭제할 계좌의 ID
     */
    void delete(@Param("id") Long id);
}