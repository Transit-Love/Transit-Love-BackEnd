package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;
    private Long matchId;
    private Long senderProfileId;
    private String senderNickname;
    private String content;
    private ChatMessage.MessageType type;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .matchId(chatMessage.getMatch().getId())
                .senderProfileId(chatMessage.getSenderProfile().getId())
                .senderNickname(chatMessage.getSenderProfile().getNickname())
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .isRead(chatMessage.getIsRead())
                .sentAt(chatMessage.getSentAt())
                .readAt(chatMessage.getReadAt())
                .build();
    }
}

