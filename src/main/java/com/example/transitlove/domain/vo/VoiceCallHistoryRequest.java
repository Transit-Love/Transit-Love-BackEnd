package com.example.transitlove.domain.vo;

import com.example.transitlove.domain.entity.CallStatus;
import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 통화 기록 저장 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceCallHistoryRequest {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long chatRoomId;

    @NotNull(message = "발신자 프로필 ID는 필수입니다.")
    private Long callerProfileId;

    @NotNull(message = "수신자 프로필 ID는 필수입니다.")
    private Long receiverProfileId;

    @NotNull(message = "통화 시작 시각은 필수입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "통화 종료 시각은 필수입니다.")
    private LocalDateTime endTime;

    @NotNull(message = "통화 시간은 필수입니다.")
    @Min(value = 0, message = "통화 시간은 0 이상이어야 합니다.")
    private Integer duration;

    @NotNull(message = "통화 상태는 필수입니다.")
    private CallStatus callStatus;

    @Builder.Default
    private Boolean voiceModulationUsed = false;
}

