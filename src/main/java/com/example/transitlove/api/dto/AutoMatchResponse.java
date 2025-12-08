package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 자동 매칭 결과 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AutoMatchResponse {

    private Long matchId;
    private ProfileSummaryDto myProfile;
    private ProfileSummaryDto partnerProfile;
    private String matchType;
    private Integer similarityPercentage;
    private LocalDateTime matchedAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileSummaryDto {
        private Long id;
        private String nickname;
        private String mbti;
    }

    public static AutoMatchResponse from(Match match, Long myProfileId) {
        boolean isProfile1 = match.getProfile1().getId().equals(myProfileId);

        return AutoMatchResponse.builder()
                .matchId(match.getId())
                .myProfile(ProfileSummaryDto.builder()
                        .id(isProfile1 ? match.getProfile1().getId() : match.getProfile2().getId())
                        .nickname(isProfile1 ? match.getProfile1().getNickname() : match.getProfile2().getNickname())
                        .mbti(isProfile1 ? match.getProfile1().getMbti() : match.getProfile2().getMbti())
                        .build())
                .partnerProfile(ProfileSummaryDto.builder()
                        .id(isProfile1 ? match.getProfile2().getId() : match.getProfile1().getId())
                        .nickname(isProfile1 ? match.getProfile2().getNickname() : match.getProfile1().getNickname())
                        .mbti(isProfile1 ? match.getProfile2().getMbti() : match.getProfile1().getMbti())
                        .build())
                .matchType(match.getMatchType().name())
                .similarityPercentage((int)(match.getSimilarityScore() * 100))
                .matchedAt(match.getMatchedAt())
                .build();
    }
}

