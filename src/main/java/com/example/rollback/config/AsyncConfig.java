package com.example.rollback.config;

import com.example.rollback.util.ContextLogger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리를 위한 설정 클래스.
 * ContextLogger를 비동기 환경에서도 사용할 수 있도록 설정합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 처리를 위한 쓰레드 풀 빈을 설정합니다.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    /**
     * 비동기 처리 중 발생하는 예외를 처리합니다.
     */
    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, java.lang.reflect.Method method, Object... params) {
                ContextLogger.error("비동기 메서드 실행 중 예외 발생 - 메서드: {}, 매개변수: {}", 
                                 method.getName(), java.util.Arrays.toString(params), ex);
            }
        };
    }
}