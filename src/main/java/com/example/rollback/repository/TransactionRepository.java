package com.example.rollback.repository;

import com.example.rollback.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 거래 데이터 접근(MyBatis) 인터페이스입니다.
 */
@Mapper
public interface TransactionRepository {
    
    /**
     * 거래를 저장합니다.
     */
    void save(Transaction transaction);
    
    /**
     * ID로 거래를 조회합니다. (없으면 null)
     */
    Transaction findById(@Param("id") Long id);
    
    /**
     * GUID로 거래를 조회합니다. (없으면 null)
     */
    Transaction findByGuid(@Param("guid") String guid);
    
    /**
     * 계좌별 거래를 조회합니다.
     */
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
    
    /**
     * 고객별 거래를 조회합니다.
     */
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 거래 전체를 조회합니다.
     */
    List<Transaction> findAll();
    
    /**
     * 거래 상태를 변경합니다.
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 거래 정보를 수정합니다.
     */
    void update(Transaction transaction);
    
    /**
     * 거래를 삭제합니다.
     */
    void delete(@Param("id") Long id);
}
