package com.example.transitlove.service;

import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.MatchRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.service.AutoMatchingDomainService;
import com.example.transitlove.domain.vo.ProfileSimilarity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 매칭 애플리케이션 서비스
 * DDD - Application Service
 * Use Case를 조율하고 트랜잭션 경계 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;
    private final AutoMatchingDomainService autoMatchingDomainService;

    /**
     * 모든 미매칭 프로필을 자동으로 매칭
     * Use Case: 전체 자동 매칭
     */
    @Transactional
    public List<Match> autoMatchAllProfiles() {
        log.info("전체 프로필 자동 매칭 시작");

        // 1. 이미 매칭된 프로필 ID 조회
        List<Match> existingMatches = matchRepository.findAll().stream()
                .filter(Match::getIsActive)
                .collect(Collectors.toList());

        List<Long> matchedProfileIds = existingMatches.stream()
                .flatMap(match -> List.of(match.getProfile1().getId(), match.getProfile2().getId()).stream())
                .distinct()
                .collect(Collectors.toList());

        log.debug("기존 매칭된 프로필 수: {}", matchedProfileIds.size());

        // 2. 미매칭 프로필 조회
        List<Profile> unmatchedProfiles = profileRepository.findAll().stream()
                .filter(profile -> !matchedProfileIds.contains(profile.getId()))
                .collect(Collectors.toList());

        log.info("미매칭 프로필 수: {}", unmatchedProfiles.size());

        if (unmatchedProfiles.size() < 2) {
            log.warn("매칭할 프로필이 부족합니다.");
            return List.of();
        }

        // 3. 도메인 서비스를 통한 최적 매칭 생성
        List<Match> newMatches = autoMatchingDomainService.createOptimalMatches(unmatchedProfiles);

        // 4. 매칭 저장
        List<Match> savedMatches = matchRepository.saveAll(newMatches);

        log.info("자동 매칭 완료 - 새로운 매칭 수: {}", savedMatches.size());
        return savedMatches;
    }

    /**
     * 특정 프로필에 대한 최적 매칭 찾기
     * Use Case: 개별 프로필 매칭
     */
    @Transactional
    public Match autoMatchProfile(Long profileId) {
        log.info("프로필 {} 자동 매칭 시작", profileId);

        // 1. 대상 프로필 조회
        Profile targetProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId));

        // 2. 이미 매칭되었는지 확인
        List<Match> existingMatches = matchRepository.findActiveMatchesByProfileId(profileId);
        if (!existingMatches.isEmpty()) {
            log.warn("프로필 {}은 이미 매칭되어 있습니다.", profileId);
            throw new IllegalStateException("이미 매칭된 프로필입니다.");
        }

        // 3. 후보 프로필 조회 (자신과 이미 매칭된 사람 제외)
        List<Long> matchedProfileIds = matchRepository.findAll().stream()
                .filter(Match::getIsActive)
                .flatMap(match -> List.of(match.getProfile1().getId(), match.getProfile2().getId()).stream())
                .distinct()
                .collect(Collectors.toList());

        List<Profile> candidateProfiles = profileRepository.findAll().stream()
                .filter(profile -> !profile.getId().equals(profileId))
                .filter(profile -> !matchedProfileIds.contains(profile.getId()))
                .collect(Collectors.toList());

        if (candidateProfiles.isEmpty()) {
            log.warn("매칭 가능한 후보가 없습니다.");
            throw new IllegalStateException("매칭 가능한 후보가 없습니다.");
        }

        // 4. 도메인 서비스를 통한 최적 매칭 찾기
        Match match = autoMatchingDomainService.findBestMatchForProfile(targetProfile, candidateProfiles)
                .orElseThrow(() -> new IllegalStateException("매칭을 생성할 수 없습니다."));

        // 5. 매칭 저장
        Match savedMatch = matchRepository.save(match);

        log.info("자동 매칭 완료 - 매칭 ID: {}, 유사도: {}%",
                savedMatch.getId(),
                (int)(savedMatch.getSimilarityScore() * 100));

        return savedMatch;
    }

    /**
     * 매칭 추천 리스트 조회 (실제 매칭하지 않음)
     * Use Case: 매칭 추천
     */
    public List<ProfileSimilarity> getMatchRecommendations(Long profileId, int limit) {
        log.debug("프로필 {} 매칭 추천 조회 - 제한: {}", profileId, limit);

        Profile targetProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId));

        // 이미 매칭된 프로필 제외
        List<Long> matchedProfileIds = matchRepository.findActiveMatchesByProfileId(profileId).stream()
                .flatMap(match -> List.of(match.getProfile1().getId(), match.getProfile2().getId()).stream())
                .filter(id -> !id.equals(profileId))
                .collect(Collectors.toList());

        List<Profile> candidateProfiles = profileRepository.findAll().stream()
                .filter(profile -> !profile.getId().equals(profileId))
                .filter(profile -> !matchedProfileIds.contains(profile.getId()))
                .collect(Collectors.toList());

        return autoMatchingDomainService.getRecommendedMatches(targetProfile, candidateProfiles, limit);
    }

    /**
     * 매칭 해제
     */
    @Transactional
    public void unmatch(Long profileId, Long matchId) {
        log.info("매칭 해제 - 프로필: {}, 매칭: {}", profileId, matchId);

        Match match = matchRepository.findActiveMatchByIdAndProfileId(matchId, profileId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없거나 권한이 없습니다."));

        match.unmatch();
        log.info("매칭 해제 완료 - 매칭 ID: {}", matchId);
    }
}

