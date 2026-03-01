package com.example.rollback.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 시간을 측정해 로그로 남깁니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MeasuredExecutionTime {

    /**
     * 로그에 표시할 작업 이름입니다.
     */
    String value();
}
