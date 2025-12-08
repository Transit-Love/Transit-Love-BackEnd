package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 마지막 선택 옵션 조회 응답 DTO
 * 현재 매칭 상대 + 채팅했던 다른 사람들
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChoiceOptionsResponse {

    private CurrentMatchDto currentMatch; // 현재 매칭 상대
    private List<ChatPartnerDto> chatPartners; // 채팅했던 다른 사람들

    /**
     * 현재 매칭 상대 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentMatchDto {
        private Long matchId;
        private Long profileId;
        private String nickname;
        private String mbti;
        private LocalDateTime matchedAt;
        private Long totalMessageCount; // 총 메시지 수
    }

    /**
     * 채팅했던 다른 사람 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatPartnerDto {
        private Long profileId;
        private String nickname;
        private String mbti;
        private Long totalMessageCount; // 총 메시지 수
        private LocalDateTime lastMessageAt; // 마지막 메시지 시간

        public static ChatPartnerDto from(Profile profile, Long messageCount, LocalDateTime lastMessageAt) {
            return ChatPartnerDto.builder()
                    .profileId(profile.getId())
                    .nickname(profile.getNickname())
                    .mbti(profile.getMbti())
                    .totalMessageCount(messageCount)
                    .lastMessageAt(lastMessageAt)
                    .build();
        }
    }
}

