package com.example.rollback.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * {@link MeasuredExecutionTime} 기반 실행 시간 측정 AOP입니다.
 */
@Slf4j
@Aspect
@Component
public class MeasuredExecutionTimeAspect {

    @Around("@annotation(measuredExecutionTime)")
    public Object measure(ProceedingJoinPoint joinPoint, MeasuredExecutionTime measuredExecutionTime)
            throws Throwable {
        long startTime = System.currentTimeMillis();
        String taskName = measuredExecutionTime.value();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} 완료 - 소요시간: {}ms", taskName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} 실패 - 소요시간: {}ms", taskName, duration, ex);
            throw new RuntimeException(ex); 
        }
    }
}
