package com.example.rollback.config;

import com.example.rollback.util.ContextHolder;
import com.example.rollback.util.CtxMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.concurrent.Executor;
import org.slf4j.MDC;

/**
 * 비동기 실행을 위한 설정입니다.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 비동기 실행용 스레드 풀입니다.
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
     * 비동기 예외를 로깅합니다.
     */
    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(@NonNull Throwable ex, @NonNull java.lang.reflect.Method method, @NonNull Object... params) {
                CtxMap context = ContextHolder.getCurrentContext();
                String guid = context.getString("guid");
                MDC.put("guid", guid);
                String message = String.format("비동기 메서드 실행 중 예외 발생 - 메서드: %s, 매개변수: %s",
                                                method.getName(), Arrays.toString(params));
                log.error(message, ex);
                MDC.remove("guid");
            }
        };
    }
}
