package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.TempChat;
import com.example.transitlove.domain.entity.TempChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 임시 채팅 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class TempChatResponse {

    private Long tempChatId;
    private ProfileSummaryDto otherProfile;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long remainingMinutes;
    private Boolean isExpired;
    private Long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileSummaryDto {
        private Long id;
        private String nickname;
        private String mbti;
    }

    public static TempChatResponse from(TempChat tempChat, Long myProfileId, Long unreadCount, TempChatMessage lastMessage) {
        var otherProfile = tempChat.getOtherProfile(myProfileId);

        return TempChatResponse.builder()
                .tempChatId(tempChat.getId())
                .otherProfile(ProfileSummaryDto.builder()
                        .id(otherProfile.getId())
                        .nickname(otherProfile.getNickname())
                        .mbti(otherProfile.getMbti())
                        .build())
                .createdAt(tempChat.getCreatedAt())
                .expiresAt(tempChat.getExpiresAt())
                .remainingMinutes(tempChat.getRemainingMinutes())
                .isExpired(tempChat.isExpired())
                .unreadCount(unreadCount)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getSentAt() : null)
                .build();
    }
}

