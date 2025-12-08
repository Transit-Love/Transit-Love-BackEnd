package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 리스트 응답 DTO
 * 매칭된 채팅방과 일반 채팅방을 구분하여 반환
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponse {

    private List<ChatRoomDto> matchedRooms;  // 매칭된 채팅방
    private List<ChatRoomDto> unmatchedRooms;  // 매칭되지 않은 채팅방
    private Integer totalCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomDto {
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
        private String lastMessage;
        private Long unreadCount;
        private Boolean isActive;

        public static ChatRoomDto from(ChatRoom chatRoom, Long myProfileId, String lastMessage, Long unreadCount) {
            var otherProfile = chatRoom.getOtherProfile(myProfileId);

            return ChatRoomDto.builder()
                    .chatRoomId(chatRoom.getId())
                    .matchId(chatRoom.getMatch() != null ? chatRoom.getMatch().getId() : null)
                    .roomType(chatRoom.getRoomType())
                    .otherProfileId(otherProfile.getId())
                    .otherNickname(otherProfile.getNickname())
                    .otherMbti(otherProfile.getMbti())
                    .createdAt(chatRoom.getCreatedAt())
                    .lastMessageAt(chatRoom.getLastMessageAt())
                    .expiresAt(chatRoom.getExpiresAt())
                    .lastMessage(lastMessage)
                    .unreadCount(unreadCount)
                    .isActive(chatRoom.getIsActive())
                    .build();
        }
    }
}

