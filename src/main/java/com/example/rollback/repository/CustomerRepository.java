package com.example.rollback.repository;

import com.example.rollback.domain.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerRepository {
    
    void save(Customer customer);
    
    Customer findById(@Param("id") Long id);
    
    Customer findByCustomerNumber(@Param("customerNumber") String customerNumber);
    
    List<Customer> findAll();
    
    void update(Customer customer);
    
    void delete(@Param("id") Long id);
}