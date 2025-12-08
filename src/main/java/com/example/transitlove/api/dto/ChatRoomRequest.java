package com.example.transitlove.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성/조회 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRequest {
    private Long targetProfileId;  // 채팅 상대방 프로필 ID
}

