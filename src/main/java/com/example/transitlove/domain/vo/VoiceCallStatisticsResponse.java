package com.example.transitlove.domain.vo;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 통화 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceCallStatisticsResponse {
    private Long chatRoomId;
    private Integer totalCalls;
    private Integer completedCalls;
    private Integer rejectedCalls;
    private Integer missedCalls;
    private Integer failedCalls;
    private Integer totalDuration;
    private Integer averageDuration;
    private Integer longestCallDuration;
    private Integer shortestCallDuration;
    private Double voiceModulationUsageRate;
    private LocalDateTime firstCallTime;
    private LocalDateTime lastCallTime;
}

