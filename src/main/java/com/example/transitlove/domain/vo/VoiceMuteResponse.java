package com.example.transitlove.domain.vo;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 음소거 상태 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceMuteResponse {
    private Long chatRoomId;
    private Long profileId;
    private Boolean isMuted;
    private LocalDateTime updatedAt;
}

