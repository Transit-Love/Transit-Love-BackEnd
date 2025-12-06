package com.example.transitlove.config;

import com.example.transitlove.domain.entity.*;
import com.example.transitlove.domain.repository.BalanceGameRepository;
import com.example.transitlove.domain.repository.KeywordRepository;
import com.example.transitlove.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final BalanceGameRepository balanceGameRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("초기 데이터 로딩 시작...");

        // User 데이터 삽입
        if (userRepository.count() == 0) {
            User testUser = User.builder()
                    .schoolEmail("test@example.com")
                    .name("테스트 사용자")
                    .role(Role.USER)
                    .build();
            userRepository.save(testUser);
            log.info("테스트 User 생성 완료: ID={}", testUser.getId());
        }

        // Keyword 데이터 삽입
        if (keywordRepository.count() == 0) {
            String[] keywordNames = {
                    "활발한", "조용한", "유머러스한", "진지한", "사교적인",
                    "내향적인", "열정적인", "차분한", "로맨틱한", "현실적인"
            };

            for (String name : keywordNames) {
                Keyword keyword = Keyword.builder()
                        .name(name)
                        .build();
                keywordRepository.save(keyword);
            }
            log.info("Keyword {} 개 생성 완료", keywordNames.length);
        }

        // BalanceGame 데이터 삽입
        if (balanceGameRepository.count() == 0) {
            String[][] balanceGames = {
                    {"데이트 장소는?", "카페에서 조용히", "놀이공원에서 신나게"},
                    {"주말에 뭐 할래?", "집에서 영화보기", "밖에 나가서 산책"},
                    {"선물을 받는다면?", "실용적인 선물", "감성적인 선물"},
                    {"여행 스타일은?", "계획적인 여행", "즉흥적인 여행"},
                    {"연락 스타일은?", "자주 연락하기", "필요할 때만 연락"}
            };

            for (String[] game : balanceGames) {
                BalanceGame balanceGame = BalanceGame.builder()
                        .question(game[0])
                        .option1(game[1])
                        .option2(game[2])
                        .build();
                balanceGameRepository.save(balanceGame);
            }
            log.info("BalanceGame {} 개 생성 완료", balanceGames.length);
        }

        log.info("초기 데이터 로딩 완료!");
    }
}
