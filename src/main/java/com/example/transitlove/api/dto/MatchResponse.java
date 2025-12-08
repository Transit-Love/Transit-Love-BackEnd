package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    private Long id;
    private ProfileSummary profile1;
    private ProfileSummary profile2;
    private Boolean isActive;
    private LocalDateTime matchedAt;
    private Long unreadCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileSummary {
        private Long id;
        private String nickname;
        private String mbti;
    }

    public static MatchResponse from(Match match, Long currentProfileId, Long unreadCount) {
        return MatchResponse.builder()
                .id(match.getId())
                .profile1(ProfileSummary.builder()
                        .id(match.getProfile1().getId())
                        .nickname(match.getProfile1().getNickname())
                        .mbti(match.getProfile1().getMbti())
                        .build())
                .profile2(ProfileSummary.builder()
                        .id(match.getProfile2().getId())
                        .nickname(match.getProfile2().getNickname())
                        .mbti(match.getProfile2().getMbti())
                        .build())
                .isActive(match.getIsActive())
                .matchedAt(match.getMatchedAt())
                .unreadCount(unreadCount)
                .build();
    }
}

