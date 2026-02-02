package com.example.rollback.config;

import com.example.rollback.retry.LockRetryTemplate;
import com.example.rollback.retry.RetryStrategy;
import com.example.rollback.retry.strategy.RandomBackoffRetryStrategy;
import com.example.rollback.retry.strategy.RetryCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class RetryConfig {

    @Bean
    public RetryStrategy retryStrategy(List<RetryCondition> conditions) {
        // 10번 재시도, 기본 100ms 대기, 최대 200ms 지터, 최대 2000ms 대기
        return new RandomBackoffRetryStrategy(10, 100, 200, 2000, conditions);
    }

    @Bean
    public LockRetryTemplate lockRetryTemplate(RetryStrategy retryStrategy) {
        return new LockRetryTemplate(retryStrategy);
    }
}