package com.example.rollback.repository;

import com.example.rollback.domain.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationLogRepository {
    void save(NotificationLog log);
    NotificationLog findById(@Param("id") Long id);
    List<NotificationLog> findAll();
    List<NotificationLog> findByAccountId(@Param("accountId") Long accountId);
    List<NotificationLog> findByTransactionId(@Param("transactionId") Long transactionId);
    List<NotificationLog> findByCustomerId(@Param("customerId") Long customerId);
}