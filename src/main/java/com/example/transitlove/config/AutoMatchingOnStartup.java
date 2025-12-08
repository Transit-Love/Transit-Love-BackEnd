package com.example.transitlove.config;

import com.example.transitlove.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 서버 시작 시 자동 매칭을 실행하는 컴포넌트
 * ApplicationReadyEvent: 서버가 완전히 준비되고 모든 빈이 초기화된 후 발생
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoMatchingOnStartup {

    private final MatchingService matchingService;

    /**
     * 서버 시작 완료 후 자동 매칭 실행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=================================================");
        log.info("서버 시작 완료 - 자동 매칭 프로세스 시작");
        log.info("=================================================");

        try {
            // 모든 미매칭 프로필에 대해 자동 매칭 실행
            var matches = matchingService.autoMatchAllProfiles();

            if (matches.isEmpty()) {
                log.info("서버 시작 시 새로운 매칭이 생성되지 않았습니다.");
                log.info("- 매칭 가능한 미매칭 프로필이 없거나");
                log.info("- 모든 프로필이 이미 매칭되어 있습니다.");
            } else {
                log.info("=================================================");
                log.info("✅ 서버 시작 시 자동 매칭 완료!");
                log.info("총 {} 쌍의 새로운 매칭이 생성되었습니다.", matches.size());
                log.info("=================================================");

                // 매칭 상세 정보 로깅
                matches.forEach(match -> {
                    log.info("매칭 생성: 프로필 {} <-> {} (유사도: {}%)",
                            match.getProfile1().getId(),
                            match.getProfile2().getId(),
                            (int)(match.getSimilarityScore() * 100));
                });
                log.info("=================================================");
            }

        } catch (Exception e) {
            log.error("=================================================");
            log.error("❌ 서버 시작 시 자동 매칭 중 오류 발생");
            log.error("오류 메시지: {}", e.getMessage());
            log.error("서버는 정상적으로 시작되었으며, 매칭은 로그인 시 재시도됩니다.");
            log.error("=================================================", e);
        }
    }
}

