package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.AutoMatchResponse;
import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.MatchRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 및 데이터 확인용 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;

    /**
     * 현재 모든 매칭 데이터 조회
     * GET /api/admin/matches
     */
    @GetMapping("/matches")
    public ResponseEntity<MatchListResponse> getAllMatches() {
        log.info("전체 매칭 데이터 조회 요청");

        List<Match> allMatches = matchRepository.findAll();
        List<Match> activeMatches = allMatches.stream()
                .filter(Match::getIsActive)
                .collect(Collectors.toList());

        List<MatchInfoDto> matchInfoList = allMatches.stream()
                .map(match -> MatchInfoDto.builder()
                        .matchId(match.getId())
                        .profile1Id(match.getProfile1().getId())
                        .profile1Nickname(match.getProfile1().getNickname())
                        .profile1Mbti(match.getProfile1().getMbti())
                        .profile2Id(match.getProfile2().getId())
                        .profile2Nickname(match.getProfile2().getNickname())
                        .profile2Mbti(match.getProfile2().getMbti())
                        .isActive(match.getIsActive())
                        .matchType(match.getMatchType().name())
                        .similarityScore(match.getSimilarityScore())
                        .similarityPercentage((int)(match.getSimilarityScore() * 100))
                        .matchedAt(match.getMatchedAt().toString())
                        .unmatchedAt(match.getUnmatchedAt() != null ? match.getUnmatchedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        MatchListResponse response = MatchListResponse.builder()
                .totalMatches(allMatches.size())
                .activeMatches(activeMatches.size())
                .inactiveMatches(allMatches.size() - activeMatches.size())
                .matches(matchInfoList)
                .build();

        log.info("매칭 데이터 조회 완료 - 전체: {}, 활성: {}", allMatches.size(), activeMatches.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 모든 프로필 조회
     * GET /api/admin/profiles
     */
    @GetMapping("/profiles")
    public ResponseEntity<ProfileListResponse> getAllProfiles() {
        log.info("전체 프로필 데이터 조회 요청");

        List<Profile> profiles = profileRepository.findAll();

        List<ProfileInfoDto> profileInfoList = profiles.stream()
                .map(profile -> ProfileInfoDto.builder()
                        .profileId(profile.getId())
                        .nickname(profile.getNickname())
                        .mbti(profile.getMbti())
                        .userId(profile.getUser().getId())
                        .userEmail(profile.getUser().getSchoolEmail())
                        .keywordCount(profile.getProfileKeywords().size())
                        .balanceGameAnswerCount(profile.getBalanceGameAnswers().size())
                        .createdAt(profile.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        ProfileListResponse response = ProfileListResponse.builder()
                .totalProfiles(profiles.size())
                .profiles(profileInfoList)
                .build();

        log.info("프로필 데이터 조회 완료 - 전체: {}", profiles.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 통계 조회
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("통계 데이터 조회 요청");

        List<Match> allMatches = matchRepository.findAll();
        List<Profile> allProfiles = profileRepository.findAll();

        long activeMatches = allMatches.stream().filter(Match::getIsActive).count();
        long autoMatches = allMatches.stream()
                .filter(m -> m.getMatchType() == Match.MatchType.AUTO_MATCHED)
                .count();
        long manualMatches = allMatches.stream()
                .filter(m -> m.getMatchType() == Match.MatchType.MANUAL)
                .count();

        // 매칭된 프로필 ID 목록
        List<Long> matchedProfileIds = allMatches.stream()
                .filter(Match::getIsActive)
                .flatMap(m -> List.of(m.getProfile1().getId(), m.getProfile2().getId()).stream())
                .distinct()
                .collect(Collectors.toList());

        long unmatchedProfiles = allProfiles.size() - matchedProfileIds.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProfiles", allProfiles.size());
        stats.put("totalMatches", allMatches.size());
        stats.put("activeMatches", activeMatches);
        stats.put("inactiveMatches", allMatches.size() - activeMatches);
        stats.put("autoMatches", autoMatches);
        stats.put("manualMatches", manualMatches);
        stats.put("matchedProfiles", matchedProfileIds.size());
        stats.put("unmatchedProfiles", unmatchedProfiles);

        if (activeMatches > 0) {
            double avgSimilarity = allMatches.stream()
                    .filter(Match::getIsActive)
                    .mapToDouble(Match::getSimilarityScore)
                    .average()
                    .orElse(0.0);
            stats.put("averageSimilarityScore", String.format("%.2f%%", avgSimilarity * 100));
        }

        log.info("통계 조회 완료");

        return ResponseEntity.ok(stats);
    }

    // ===== Response DTOs =====

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MatchListResponse {
        private Integer totalMatches;
        private Integer activeMatches;
        private Integer inactiveMatches;
        private List<MatchInfoDto> matches;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MatchInfoDto {
        private Long matchId;
        private Long profile1Id;
        private String profile1Nickname;
        private String profile1Mbti;
        private Long profile2Id;
        private String profile2Nickname;
        private String profile2Mbti;
        private Boolean isActive;
        private String matchType;
        private Double similarityScore;
        private Integer similarityPercentage;
        private String matchedAt;
        private String unmatchedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileListResponse {
        private Integer totalProfiles;
        private List<ProfileInfoDto> profiles;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileInfoDto {
        private Long profileId;
        private String nickname;
        private String mbti;
        private Long userId;
        private String userEmail;
        private Integer keywordCount;
        private Integer balanceGameAnswerCount;
        private String createdAt;
    }
}

