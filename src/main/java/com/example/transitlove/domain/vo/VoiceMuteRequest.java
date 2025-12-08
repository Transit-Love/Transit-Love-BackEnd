package com.example.transitlove.domain.vo;

import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * 음소거 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceMuteRequest {

    @NotNull(message = "프로필 ID는 필수입니다.")
    private Long profileId;

    @NotNull(message = "음소거 상태는 필수입니다.")
    private Boolean isMuted;

    @NotNull(message = "타임스탬프는 필수입니다.")
    private Long timestamp;
}

