package com.example.rollback.repository;

import com.example.rollback.domain.Account;
import com.example.rollback.domain.AccountStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 계좌 테이블 접근을 담당합니다. (MyBatis)
 */
@Mapper
public interface AccountRepository {
    
    /**
     * 계좌를 조회합니다.
     */
    Account findById(@Param("id") Long id);

    /**
     * 계좌를 조회하면서 락을 획득합니다. (SKIP LOCKED)
     */
    Account findByIdForUpdateSkipLocked(@Param("id") Long id);
    
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
    void updateStatus(@Param("id") Long id, @Param("status") AccountStatus status);
    
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
