package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.TempChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 임시 채팅 메시지 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class TempChatMessageResponse {

    private Long id;
    private Long tempChatId;
    private Long senderProfileId;
    private String senderNickname;
    private String content;
    private String type;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    public static TempChatMessageResponse from(TempChatMessage message) {
        return TempChatMessageResponse.builder()
                .id(message.getId())
                .tempChatId(message.getTempChat().getId())
                .senderProfileId(message.getSenderProfile().getId())
                .senderNickname(message.getSenderProfile().getNickname())
                .content(message.getContent())
                .type(message.getType().name())
                .isRead(message.getIsRead())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .build();
    }
}

