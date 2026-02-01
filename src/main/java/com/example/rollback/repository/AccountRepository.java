package com.example.rollback.repository;

import com.example.rollback.domain.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountRepository {
    
    void save(Account account);
    
    Account findById(@Param("id") Long id);
    
    Account findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    List<Account> findByCustomerId(@Param("customerId") Long customerId);
    
    List<Account> findAll();
    
    void updateBalance(Account account);
    
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    void update(Account account);
    
    void delete(@Param("id") Long id);
}