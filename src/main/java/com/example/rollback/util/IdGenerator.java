package com.example.rollback.util;

/**
 * 도메인별 고유 ID를 생성하는 유틸리티 클래스.
 */
public final class IdGenerator {

    // 인스턴스화 방지
    private IdGenerator() {}

    /**
     * 지정된 접두사를 사용하여 고유 ID를 생성합니다.
     * 예: "ACC" + 현재시간(ms) + 4자리 난수
     *
     * @param prefix ID 접두사 (예: "ACC", "CUST", "TXN")
     * @return 생성된 고유 ID
     */
    public static String generate(String prefix) {
        // 단순 난수(요구사항 범위)
        String timePart = String.valueOf(System.currentTimeMillis());
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + timePart + randomPart;
    }
}
