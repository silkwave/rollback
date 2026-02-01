package com.example.rollback.config;

import com.example.rollback.retry.LinearBackoffRetryStrategy;
import com.example.rollback.retry.LockRetryTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfig {

    @Bean
    public LinearBackoffRetryStrategy linearBackoffRetryStrategy() {
        return new LinearBackoffRetryStrategy(5, 1000, 500);
    }

    @Bean
    public LockRetryTemplate lockRetryTemplate(LinearBackoffRetryStrategy retryStrategy) {
        return new LockRetryTemplate(retryStrategy);
    }
}