package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 상세 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long chatRoomId;
    private Long matchId;  // 매칭된 경우만
    private ChatRoom.RoomType roomType;

    // 상대방 정보
    private Long otherProfileId;
    private String otherNickname;
    private String otherMbti;

    // 채팅방 정보
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime expiresAt; // 만료 시간 (UNMATCHED만)
    private Boolean isActive;

    public static ChatRoomResponse from(ChatRoom chatRoom, Long myProfileId) {
        var otherProfile = chatRoom.getOtherProfile(myProfileId);

        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .matchId(chatRoom.getMatch() != null ? chatRoom.getMatch().getId() : null)
                .roomType(chatRoom.getRoomType())
                .otherProfileId(otherProfile.getId())
                .otherNickname(otherProfile.getNickname())
                .otherMbti(otherProfile.getMbti() != null ? otherProfile.getMbti().getValue() : null)
                .createdAt(chatRoom.getCreatedAt())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .expiresAt(chatRoom.getExpiresAt())
                .isActive(chatRoom.getIsActive())
                .build();
    }
}

