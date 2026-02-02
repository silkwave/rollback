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
 * 비동기 처리를 위한 Spring 설정 클래스
 * 
 * <p>이 클래스는 Spring 애플리케이션에서 비동기 처리를 위한 설정을 담당합니다.
 * ContextLogger를 비동기 환경에서도 일관되게 사용할 수 있도록 커스텀 쓰레드 풀과
 * 예외 처리기를 설정합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>커스텀 쓰레드 풀 빈 설정 및 관리</li>
 *   <li>비동기 메서드 실행 중 발생하는 예외의 중앙 처리</li>
 *   <li>컨텍스트 정보(GUID 등)를 비동기 환경에서도 유지</li>
 * </ul>
 * 
 * @author Spring Application Team
 * @version 1.0
 * @since 2024.01.01
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 비동기 처리를 위한 커스텀 쓰레드 풀 빈을 생성하고 설정합니다.
     * 
     * <p>이 메서드는 Spring의 {@link ThreadPoolTaskExecutor}를 사용하여
     * 다음과 같은 설정으로 쓰레드 풀을 구성합니다:</p>
     * <ul>
     *   <li>코어 쓰레드 수: 2개 (항상 활성 상태로 유지되는 기본 쓰레드)</li>
     *   <li>최대 쓰레드 수: 5개 (부하 시 확장 가능한 최대 쓰레드)</li>
     *   <li>큐 용량: 100개 (처리 대기 중인 작업 저장 공간)</li>
     *   <li>쓰레드 이름 접두사: "Async-" (로깅 및 모니터링 용이)</li>
     * </ul>
     * 
     * <p>이 설정은 애플리케이션의 비동기 작업 부하를 효율적으로 처리하고
     * 시스템 리소스를 적절히 관리하기 위해 최적화되었습니다.</p>
     * 
     * @return 설정이 완료된 {@link Executor} 빈 객체
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
     * 비동기 메서드 실행 중 발생하는 처리되지 않은 예외를 처리하는 핸들러 빈을 생성합니다.
     * 
     * <p>이 핸들러는 비동기 환경에서 발생하는 예외를 중앙에서 처리하여
     * 다음과 같은 기능을 제공합니다:</p>
     * <ul>
     *   <li>현재 컨텍스트의 GUID 정보 추출 및 로깅</li>
     *   <li>MDC(Mapped Diagnostic Context)에 GUID 설정하여 로그 추적성 확보</li>
     *   <li>예외 발생 메서드 정보 및 매개변수 상세 로깅</li>
     *   <li>예외 처리 후 MDC 정리로 메모리 누수 방지</li>
     * </ul>
     * 
     * <p>이를 통해 비동기 환경에서도 동기 환경과 동일한 수준의
     * 로깅 및 예외 처리 품질을 유지할 수 있습니다.</p>
     * 
     * @return 비동기 예외 처리를 위한 {@link AsyncUncaughtExceptionHandler} 빈 객체
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