package com.example.transitlove.api.dto;

import com.example.transitlove.domain.vo.ProfileSimilarity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 매칭 추천 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class MatchRecommendationResponse {

    private Long profileId;
    private String nickname;
    private String mbti;
    private Integer similarityPercentage;
    private Integer mbtiScore;
    private Integer balanceGameScore;

    public static MatchRecommendationResponse from(ProfileSimilarity similarity) {
        return MatchRecommendationResponse.builder()
                .profileId(similarity.getProfile2().getId())
                .nickname(similarity.getProfile2().getNickname())
                .mbti(similarity.getProfile2().getMbti())
                .similarityPercentage(similarity.getTotalScorePercentage())
                .mbtiScore((int)(similarity.getMbtiScore() * 100))
                .balanceGameScore((int)(similarity.getBalanceGameScore() * 100))
                .build();
    }
}

