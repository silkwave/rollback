package com.example.rollback.config;

import com.example.rollback.retry.RetryStrategy;
import com.example.rollback.retry.strategy.RandomBackoffRetryStrategy;
import com.example.rollback.retry.strategy.RetryCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 재시도 전략을 구성합니다.
 */
@Configuration
public class RetryConfig {

    /**
     * 랜덤 백오프 기반 재시도 전략을 생성합니다.
     */
    @Bean
    public RetryStrategy retryStrategy(RetryCondition retryCondition) {
        // 기본값: 10회, 100ms+지터(<=200ms), 최대 2s
        return new RandomBackoffRetryStrategy(10, 100, 200, 2000, retryCondition);
    }
}
