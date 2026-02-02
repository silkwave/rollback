package com.example.rollback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 뱅킹 시스템 메인 애플리케이션 클래스
 * 
 * <p>이 클래스는 Spring Boot 기반의 뱅킹 시스템을 시작하는 엔트리 포인트입니다.
 * 트랜잭션 롤백, 이벤트 기반 아키텍처, 재시도 메커니즘 등의 기능을 제공하는
 * 엔터프라이즈 금융 시스템의 데모 애플리케이션입니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>계좌 관리 (개설, 동결, 활성화)</li>
 *   <li>거래 처리 (입금, 이체, 롤백)</li>
 *   <li>고객 관리 (등록, 정보 수정, 상태 관리)</li>
 *   <li>이벤트 기반 알림 시스템</li>
 *   <li>GUID 기반 요청 추적</li>
 *   <li>전략 패턴 기반 재시도 메커니즘</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2026-02-02
 */
@SpringBootApplication
public class RollbackApplication {
    
    /**
     * Spring Boot 애플리케이션을 실행하는 메인 메서드
     * 
     * @param args 커맨드 라인 인자 (현재는 사용하지 않음)
     */
    public static void main(String[] args) {
        SpringApplication.run(RollbackApplication.class, args);
    }
}