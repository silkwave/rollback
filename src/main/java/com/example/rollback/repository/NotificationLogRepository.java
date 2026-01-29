package com.example.rollback.repository;

import com.example.rollback.domain.NotificationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationLogRepository {
    void save(NotificationLog log);
}