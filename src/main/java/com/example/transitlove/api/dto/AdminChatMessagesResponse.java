package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatMessage;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 어드민 전용 - 특정 커플의 채팅 메시지 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChatMessagesResponse {

    private Long matchId;
    private MatchProfilesDto matchProfiles;
    private List<MessageDto> messages;
    private long totalMessageCount;

    /**
     * 매칭된 프로필 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchProfilesDto {
        private ProfileDto profile1;
        private ProfileDto profile2;
        private LocalDateTime matchedAt;
        private Boolean isActive;
    }

    /**
     * 프로필 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileDto {
        private Long profileId;
        private String nickname;
        private String mbti;
    }

    /**
     * 메시지 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDto {
        private Long messageId;
        private Long senderProfileId;
        private String senderNickname;
        private String content;
        private String type;
        private Boolean isRead;
        private LocalDateTime sentAt;
        private LocalDateTime readAt;

        public static MessageDto from(ChatMessage chatMessage) {
            return MessageDto.builder()
                    .messageId(chatMessage.getId())
                    .senderProfileId(chatMessage.getSenderProfile().getId())
                    .senderNickname(chatMessage.getSenderProfile().getNickname())
                    .content(chatMessage.getContent())
                    .type(chatMessage.getType().toString())
                    .isRead(chatMessage.getIsRead())
                    .sentAt(chatMessage.getSentAt())
                    .readAt(chatMessage.getReadAt())
                    .build();
        }
    }
}

