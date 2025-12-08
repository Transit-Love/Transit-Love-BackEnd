package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.AutoMatchResponse;
import com.example.transitlove.api.dto.MatchRecommendationResponse;
import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.domain.vo.ProfileSimilarity;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 자동 매칭 컨트롤러
 * DDD - Presentation Layer (Interface Adapter)
 */
@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * 전체 프로필 자동 매칭 (관리자용)
     * POST /api/matching/auto-match-all
     */
    @PostMapping("/auto-match-all")
    public ResponseEntity<List<AutoMatchResponse>> autoMatchAllProfiles(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("전체 자동 매칭 요청");

        List<Match> matches = matchingService.autoMatchAllProfiles();

        List<AutoMatchResponse> responses = matches.stream()
                .map(match -> AutoMatchResponse.from(match, match.getProfile1().getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 내 프로필 자동 매칭
     * POST /api/matching/auto-match
     */
    @PostMapping("/auto-match")
    public ResponseEntity<AutoMatchResponse> autoMatchMyProfile(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("개별 자동 매칭 요청 - 사용자: {}", oauth2User.getName());

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        Match match = matchingService.autoMatchProfile(profileId);

        AutoMatchResponse response = AutoMatchResponse.from(match, profileId);

        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 추천 목록 조회
     * GET /api/matching/recommendations?limit=10
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<MatchRecommendationResponse>> getMatchRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("매칭 추천 조회 - 사용자: {}, 제한: {}", oauth2User.getName(), limit);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        List<ProfileSimilarity> recommendations = matchingService.getMatchRecommendations(profileId, limit);

        List<MatchRecommendationResponse> responses = recommendations.stream()
                .map(MatchRecommendationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 매칭 해제
     * DELETE /api/matching/{matchId}
     */
    @DeleteMapping("/{matchId}")
    public ResponseEntity<String> unmatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("매칭 해제 요청 - 사용자: {}, 매칭: {}", oauth2User.getName(), matchId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        matchingService.unmatch(profileId, matchId);

        return ResponseEntity.ok("매칭이 해제되었습니다.");
    }

    // ===== Helper Methods =====

    private Long getProfileIdFromOAuth2User(CustomOAuth2User oauth2User) {
        User user = userRepository.findBySchoolEmail(oauth2User.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        return profile.getId();
    }
}

