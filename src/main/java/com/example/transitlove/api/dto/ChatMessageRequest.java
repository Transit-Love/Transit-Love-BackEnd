package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    private Long matchId;
    private String content;
    private ChatMessage.MessageType type;
}

