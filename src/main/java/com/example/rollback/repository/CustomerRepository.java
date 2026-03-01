package com.example.rollback.repository;

import com.example.rollback.domain.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 고객 데이터 접근(MyBatis) 인터페이스입니다.
 */
@Mapper
public interface CustomerRepository {
    
    /**
     * 고객을 저장합니다.
     */
    void save(Customer customer);
    
    /**
     * ID로 고객을 조회합니다. (없으면 null)
     */
    Customer findById(@Param("id") Long id);
    
    /**
     * 고객번호로 고객을 조회합니다. (없으면 null)
     */
    Customer findByCustomerNumber(@Param("customerNumber") String customerNumber);
    
    /**
     * 고객 전체를 조회합니다.
     */
    List<Customer> findAll();
    
    /**
     * 고객 정보를 수정합니다.
     */
    void update(Customer customer);
    
    /**
     * 고객을 삭제합니다.
     */
    void delete(@Param("id") Long id);
}
