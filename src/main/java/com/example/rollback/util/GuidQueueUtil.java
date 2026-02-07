package com.example.rollback.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GuidQueue에 대한 정적 접근을 제공하는 유틸리티 클래스입니다.
 * GuidQueue의 복잡성을 숨기고 간단한 인터페이스를 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuidQueueUtil {

    // Spring 컨테이너로부터 GuidQueue Bean을 주입받음
    private final GuidQueue guidQueue;

    /**
     * GuidQueue에서 새로운 GUID를 가져옵니다.
     * static 메서드에서 인스턴스 메서드로 변경합니다.
     * 
     * @return 생성된 GUID 문자열
     * @throws InterruptedException 큐에서 GUID를 가져오는 동안 스레드가 중단될 경우
     */
    public String getGUID()  {

        try {
            log.debug("GUID를 큐에서 가져오는 중...");
            String guid = guidQueue.getGUID();
            log.debug("큐에서 GUID를 성공적으로 가져왔습니다: {}", guid);
            return guid;
        } catch (InterruptedException ex) {
            log.error("GUID를 큐에서 가져오는 중 인터럽트 발생", ex.getClass().getSimpleName());
            Thread.currentThread().interrupt();
            throw new RuntimeException("GUID 생성 중 인터럽트 발생", ex);
        }

    }

}