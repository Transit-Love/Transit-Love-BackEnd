package com.example.transitlove.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 목록 응답 DTO
 * - 매칭된 커플 프로필 (1개)
 * - 일반 프로필 목록 (매칭되지 않은 프로필들)
 */
@Getter
@Builder
@AllArgsConstructor
public class ChatListResponse {

    /**
     * 매칭된 커플 프로필 (있는 경우)
     */
    private MatchedProfileDto matchedProfile;

    /**
     * 일반 프로필 목록 (매칭되지 않은 프로필들)
     */
    private List<ProfileListItemDto> otherProfiles;

    /**
     * 매칭된 프로필 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class MatchedProfileDto {
        private Long matchId;
        private Long profileId;
        private String nickname;
        private String mbti;
        private LocalDateTime matchedAt;
        private Long unreadMessageCount;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
    }

    /**
     * 프로필 목록 아이템 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileListItemDto {
        private Long profileId;
        private String nickname;
        private String mbti;
        private LocalDateTime createdAt;
    }
}

