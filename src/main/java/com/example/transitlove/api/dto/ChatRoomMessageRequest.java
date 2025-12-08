package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 기반 메시지 전송 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMessageRequest {
    private Long chatRoomId;
    private String content;
    private ChatMessage.MessageType type;
}

