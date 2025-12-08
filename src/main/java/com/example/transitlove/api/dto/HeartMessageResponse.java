package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.HeartMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class HeartMessageResponse {

    private Long id;
    private SenderProfileDto senderProfile;
    private ReceiverProfileDto receiverProfile;
    private String content;
    private LocalDateTime sentAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SenderProfileDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReceiverProfileDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }

    public static HeartMessageResponse from(HeartMessage heartMessage) {
        return HeartMessageResponse.builder()
                .id(heartMessage.getId())
                .senderProfile(SenderProfileDto.builder()
                        .id(heartMessage.getSenderProfile().getId())
                        .nickname(heartMessage.getSenderProfile().getNickname())
                        .profileImageUrl(heartMessage.getSenderProfile().getProfileImageUrl())
                        .build())
                .receiverProfile(ReceiverProfileDto.builder()
                        .id(heartMessage.getReceiverProfile().getId())
                        .nickname(heartMessage.getReceiverProfile().getNickname())
                        .profileImageUrl(heartMessage.getReceiverProfile().getProfileImageUrl())
                        .build())
                .content(heartMessage.getContent())
                .sentAt(heartMessage.getSentAt())
                .build();
    }
}
