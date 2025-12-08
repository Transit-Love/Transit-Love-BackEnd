package com.example.transitlove.domain.vo;

import com.example.transitlove.domain.entity.VoiceCallHistory;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 통화 기록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceCallHistoryResponse {
    private Long callHistoryId;
    private Long chatRoomId;
    private Long callerProfileId;
    private String callerNickname;
    private Long receiverProfileId;
    private String receiverNickname;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private String callStatus;
    private Boolean voiceModulationUsed;
    private LocalDateTime createdAt;

    public static VoiceCallHistoryResponse from(VoiceCallHistory history, String callerNickname, String receiverNickname) {
        return VoiceCallHistoryResponse.builder()
                .callHistoryId(history.getId())
                .chatRoomId(history.getChatRoomId())
                .callerProfileId(history.getCallerProfileId())
                .callerNickname(callerNickname)
                .receiverProfileId(history.getReceiverProfileId())
                .receiverNickname(receiverNickname)
                .startTime(history.getStartTime())
                .endTime(history.getEndTime())
                .duration(history.getDuration())
                .callStatus(history.getCallStatus().name())
                .voiceModulationUsed(history.getVoiceModulationUsed())
                .createdAt(history.getCreatedAt())
                .build();
    }
}

