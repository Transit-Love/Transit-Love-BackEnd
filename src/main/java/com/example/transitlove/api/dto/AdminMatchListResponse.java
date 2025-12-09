package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatMessage;
import com.example.transitlove.domain.entity.Match;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 어드민 전용 - 모든 커플 매칭 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMatchListResponse {

    private List<MatchInfoDto> matches;
    private long totalCount;

    /**
     * 매칭 정보 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchInfoDto {
        private Long matchId;
        private ProfileInfoDto profile1;
        private ProfileInfoDto profile2;
        private Boolean isActive;
        private String matchType;
        private Double similarityScore;
        private LocalDateTime matchedAt;
        private LocalDateTime unmatchedAt;
        private Long totalMessageCount;
        private String lastMessage;
        private LocalDateTime lastMessageTime;

        public static MatchInfoDto from(Match match, Long messageCount, ChatMessage lastMessage) {
            return MatchInfoDto.builder()
                    .matchId(match.getId())
                    .profile1(ProfileInfoDto.builder()
                            .profileId(match.getProfile1().getId())
                            .nickname(match.getProfile1().getNickname())
                            .mbti(match.getProfile1().getMbti() != null ? match.getProfile1().getMbti().getValue() : null)
                            .build())
                    .profile2(ProfileInfoDto.builder()
                            .profileId(match.getProfile2().getId())
                            .nickname(match.getProfile2().getNickname())
                            .mbti(match.getProfile2().getMbti() != null ? match.getProfile2().getMbti().getValue() : null)
                            .build())
                    .isActive(match.getIsActive())
                    .matchType(match.getMatchType().toString())
                    .similarityScore(match.getSimilarityScore())
                    .matchedAt(match.getMatchedAt())
                    .unmatchedAt(match.getUnmatchedAt())
                    .totalMessageCount(messageCount)
                    .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                    .lastMessageTime(lastMessage != null ? lastMessage.getSentAt() : null)
                    .build();
        }
    }

    /**
     * 프로필 정보 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfoDto {
        private Long profileId;
        private String nickname;
        private String mbti;
    }
}

