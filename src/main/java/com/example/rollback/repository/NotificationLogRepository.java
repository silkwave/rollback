package com.example.rollback.repository;

import com.example.rollback.domain.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 알림 로그 데이터 접근(MyBatis) 인터페이스입니다.
 */
@Mapper
public interface NotificationLogRepository {
    
    /**
     * 알림 로그를 저장합니다.
     */
    void save(NotificationLog log);
    
    /**
     * ID로 알림 로그를 조회합니다. (없으면 null)
     */
    NotificationLog findById(@Param("id") Long id);
    
    /**
     * 알림 로그 전체를 조회합니다.
     */
    List<NotificationLog> findAll();
}
