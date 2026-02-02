package com.example.rollback.repository;

import com.example.rollback.domain.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 고객 리포지토리 인터페이스
 * 
 * <p>MyBatis를 사용하여 고객 데이터에 대한 CRUD 작업을 수행합니다.
 * 데이터베이스의 customers 테이블과 매핑되며, 고객 관련 모든 데이터 접근 로직을 제공합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>고객 생성, 조회, 수정, 삭제</li>
 *   <li>고객번호 기반 검색</li>
 *   <li>전체 고객 목록 조회</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@Mapper
public interface CustomerRepository {
    
    /**
     * 새로운 고객을 저장합니다.
     * 
     * @param customer 저장할 고객 정보
     */
    void save(Customer customer);
    
    /**
     * ID로 고객을 조회합니다.
     * 
     * @param id 조회할 고객의 ID
     * @return 고객 정보 (존재하지 않는 경우 null)
     */
    Customer findById(@Param("id") Long id);
    
    /**
     * 고객번호로 고객을 조회합니다.
     * 
     * @param customerNumber 조회할 고객번호
     * @return 고객 정보 (존재하지 않는 경우 null)
     */
    Customer findByCustomerNumber(@Param("customerNumber") String customerNumber);
    
    /**
     * 모든 고객 목록을 조회합니다.
     * 
     * @return 전체 고객 목록
     */
    List<Customer> findAll();
    
    /**
     * 고객 정보를 업데이트합니다.
     * 
     * @param customer 업데이트할 고객 정보
     */
    void update(Customer customer);
    
    /**
     * 고객을 삭제합니다.
     * 
     * @param id 삭제할 고객의 ID
     */
    void delete(@Param("id") Long id);
}