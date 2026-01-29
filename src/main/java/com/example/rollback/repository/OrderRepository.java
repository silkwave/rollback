package com.example.rollback.repository;

import com.example.rollback.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderRepository {
    
    void save(Order order);
    
    Order findById(@Param("id") Long id);
    
    List<Order> findAll();
    
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}