package com.example.rollback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync // @Async 어노테이션을 사용하기 위해 비동기 처리를 활성화합니다.
public class RollbackApplication {
    public static void main(String[] args) {
        SpringApplication.run(RollbackApplication.class, args);
        log.info("🚀 RollbackApplication started successfully with Async enabled.");
    }
}