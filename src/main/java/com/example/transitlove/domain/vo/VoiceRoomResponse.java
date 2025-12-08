package com.example.transitlove.domain.vo;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 음성 채팅방 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceRoomResponse {

    /**
     * 채팅방 ID
     */
    private Long chatRoomId;

    /**
     * 내 프로필 ID
     */
    private Long myProfileId;

    /**
     * 상대방 프로필 ID
     */
    private Long partnerProfileId;

    /**
     * 상대방 닉네임
     */
    private String partnerNickname;

    /**
     * 음성 채팅 가능 여부
     */
    private Boolean voiceEnabled;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
}

