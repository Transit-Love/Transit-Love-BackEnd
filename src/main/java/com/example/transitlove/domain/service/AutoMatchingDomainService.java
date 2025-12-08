package com.example.transitlove.domain.service;

import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.vo.ProfileSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 자동 매칭 도메인 서비스
 * DDD - Domain Service
 * 두 개 이상의 Aggregate를 협력시켜 비즈니스 로직 처리
 */
@Slf4j
@Service
public class AutoMatchingDomainService {

    /**
     * 프로필 리스트에서 최적의 커플 매칭 생성
     * @param profiles 매칭할 프로필 리스트
     * @return 생성된 매칭 리스트
     */
    public List<Match> createOptimalMatches(List<Profile> profiles) {
        log.info("자동 매칭 시작 - 프로필 수: {}", profiles.size());

        if (profiles.size() < 2) {
            log.warn("매칭할 프로필이 부족합니다. 최소 2명 필요");
            return Collections.emptyList();
        }

        // 1. 모든 프로필 쌍의 유사도 계산
        List<ProfileSimilarity> similarities = calculateAllSimilarities(profiles);

        // 2. 유사도 높은 순으로 정렬
        similarities.sort((s1, s2) -> Double.compare(s2.getTotalScore(), s1.getTotalScore()));

        // 3. 그리디 알고리즘으로 최적 매칭 생성
        List<Match> matches = createMatchesGreedy(similarities);

        log.info("자동 매칭 완료 - 생성된 매칭 수: {}", matches.size());
        return matches;
    }

    /**
     * 특정 프로필에 대한 최적의 매칭 상대 찾기
     */
    public Optional<Match> findBestMatchForProfile(Profile targetProfile, List<Profile> candidateProfiles) {
        log.debug("프로필 {}에 대한 최적 매칭 찾기", targetProfile.getId());

        if (candidateProfiles.isEmpty()) {
            return Optional.empty();
        }

        ProfileSimilarity bestSimilarity = candidateProfiles.stream()
                .map(candidate -> ProfileSimilarity.calculate(targetProfile, candidate))
                .max(Comparator.comparingDouble(ProfileSimilarity::getTotalScore))
                .orElse(null);

        if (bestSimilarity == null) {
            return Optional.empty();
        }

        Match match = Match.builder()
                .profile1(targetProfile)
                .profile2(bestSimilarity.getProfile2())
                .matchType(Match.MatchType.AUTO_MATCHED)
                .similarityScore(bestSimilarity.getTotalScore())
                .isActive(true)
                .build();

        log.info("최적 매칭 발견 - 프로필 {} <-> {} (유사도: {}%)",
                targetProfile.getId(),
                bestSimilarity.getProfile2().getId(),
                bestSimilarity.getTotalScorePercentage());

        return Optional.of(match);
    }

    /**
     * 모든 프로필 쌍의 유사도 계산
     */
    private List<ProfileSimilarity> calculateAllSimilarities(List<Profile> profiles) {
        List<ProfileSimilarity> similarities = new ArrayList<>();

        for (int i = 0; i < profiles.size(); i++) {
            for (int j = i + 1; j < profiles.size(); j++) {
                ProfileSimilarity similarity = ProfileSimilarity.calculate(
                        profiles.get(i),
                        profiles.get(j)
                );
                similarities.add(similarity);

                log.debug("유사도 계산 - 프로필 {} <-> {}: {}%",
                        profiles.get(i).getId(),
                        profiles.get(j).getId(),
                        similarity.getTotalScorePercentage());
            }
        }

        return similarities;
    }

    /**
     * 그리디 알고리즘으로 매칭 생성
     * 유사도가 높은 쌍부터 매칭하되, 이미 매칭된 프로필은 제외
     */
    private List<Match> createMatchesGreedy(List<ProfileSimilarity> similarities) {
        List<Match> matches = new ArrayList<>();
        Set<Long> matchedProfileIds = new HashSet<>();

        for (ProfileSimilarity similarity : similarities) {
            Long profile1Id = similarity.getProfile1().getId();
            Long profile2Id = similarity.getProfile2().getId();

            // 이미 매칭된 프로필인지 확인
            if (matchedProfileIds.contains(profile1Id) || matchedProfileIds.contains(profile2Id)) {
                continue;
            }

            // 매칭 생성
            Match match = Match.builder()
                    .profile1(similarity.getProfile1())
                    .profile2(similarity.getProfile2())
                    .matchType(Match.MatchType.AUTO_MATCHED)
                    .similarityScore(similarity.getTotalScore())
                    .isActive(true)
                    .build();

            matches.add(match);
            matchedProfileIds.add(profile1Id);
            matchedProfileIds.add(profile2Id);

            log.info("매칭 생성 - 프로필 {} <-> {} (유사도: {}%)",
                    profile1Id, profile2Id, similarity.getTotalScorePercentage());
        }

        return matches;
    }

    /**
     * 추천 매칭 리스트 생성 (실제 매칭하지 않고 추천만)
     */
    public List<ProfileSimilarity> getRecommendedMatches(Profile targetProfile, List<Profile> candidateProfiles, int limit) {
        return candidateProfiles.stream()
                .map(candidate -> ProfileSimilarity.calculate(targetProfile, candidate))
                .sorted((s1, s2) -> Double.compare(s2.getTotalScore(), s1.getTotalScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}

