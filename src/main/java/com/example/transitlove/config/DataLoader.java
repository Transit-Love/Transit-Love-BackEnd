package com.example.transitlove.config;

import com.example.transitlove.domain.entity.*;
import com.example.transitlove.domain.repository.BalanceGameRepository;
import com.example.transitlove.domain.repository.KeywordRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final KeywordRepository keywordRepository;
    private final BalanceGameRepository balanceGameRepository;
    private final MatchingService matchingService;

    @Override
    public void run(String... args) throws Exception {
        log.info("초기 데이터 로딩 시작...");

        // Keywords 생성
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

        // BalanceGame 생성
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

        // 테스트 Users와 Profiles 생성
        if (userRepository.count() == 0) {
            // 관리자 계정 생성 (2개)
            User admin1 = User.builder()
                    .schoolEmail("admin@transitlove.com")
                    .name("관리자")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin1);
            log.info("관리자 계정 생성 완료: admin@transitlove.com");

            User admin2 = User.builder()
                    .schoolEmail("24.058@bssm.hs.kr")
                    .name("관리자2")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin2);
            log.info("관리자 계정 생성 완료: 24.058@bssm.hs.kr");

            String[][] testUsers = {
                    {"test1@example.com", "김철수", "ENFP"},
                    {"test2@example.com", "이영희", "INFP"},
                    {"test3@example.com", "박민수", "ENFJ"},
                    {"test4@example.com", "최지은", "ISTJ"},
                    {"test5@example.com", "정호영", "ENTP"},
                    {"test6@example.com", "강서연", "INFJ"}
            };

            List<BalanceGame> balanceGames = balanceGameRepository.findAll();

            for (int i = 0; i < testUsers.length; i++) {
                String[] userData = testUsers[i];

                // User 생성
                User user = User.builder()
                        .schoolEmail(userData[0])
                        .name(userData[1])
                        .role(Role.USER)
                        .build();
                user = userRepository.save(user);

                // Profile 생성
                Profile profile = Profile.builder()
                        .user(user)
                        .nickname(userData[1])
                        .mbti(userData[2])
                        .build();
                profile = profileRepository.save(profile);

                // BalanceGame 답변 추가 (각 프로필마다 다르게)
                for (int j = 0; j < balanceGames.size(); j++) {
                    BalanceGame game = balanceGames.get(j);

                    // 프로필마다 다른 답변 패턴 생성
                    int answer;
                    if (i < 2) {
                        // 처음 2명은 비슷한 답변
                        answer = (j % 2 == 0) ? 1 : 2;
                    } else if (i < 4) {
                        // 다음 2명도 비슷한 답변
                        answer = (j % 2 == 0) ? 2 : 1;
                    } else {
                        // 나머지는 다양한 답변
                        answer = ((i + j) % 2 == 0) ? 1 : 2;
                    }

                    BalanceGameAnswer balanceAnswer = BalanceGameAnswer.builder()
                            .profile(profile)
                            .balanceGame(game)
                            .selectedOption(answer)
                            .build();
                    profile.addBalanceGameAnswer(balanceAnswer);
                }

                profileRepository.save(profile);
                log.info("테스트 User 및 Profile 생성 완료: {} ({})", userData[1], userData[2]);
            }

            log.info("총 {} 개의 테스트 프로필 생성 완료", testUsers.length);
        }

        // 자동 매칭 실행
        try {
            log.info("자동 매칭 시작...");
            List<Match> matches = matchingService.autoMatchAllProfiles();
            log.info("자동 매칭 완료! {} 개의 커플이 생성되었습니다.", matches.size());

            matches.forEach(match -> {
                log.info("💑 매칭: {} ({}) ❤️ {} ({}) - 유사도: {}%",
                        match.getProfile1().getNickname(),
                        match.getProfile1().getMbti(),
                        match.getProfile2().getNickname(),
                        match.getProfile2().getMbti(),
                        (int)(match.getSimilarityScore() * 100));
            });
        } catch (Exception e) {
            log.warn("자동 매칭 실패: {}", e.getMessage());
        }

        log.info("초기 데이터 로딩 완료!");
    }
}
