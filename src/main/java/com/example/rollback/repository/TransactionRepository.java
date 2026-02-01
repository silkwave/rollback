package com.example.rollback.repository;

import com.example.rollback.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionRepository {
    
    void save(Transaction transaction);
    
    Transaction findById(@Param("id") Long id);
    
    Transaction findByGuid(@Param("guid") String guid);
    
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
    
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);
    
    List<Transaction> findAll();
    
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    void update(Transaction transaction);
    
    void delete(@Param("id") Long id);
}