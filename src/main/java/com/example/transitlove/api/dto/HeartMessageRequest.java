package com.example.transitlove.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeartMessageRequest {

    @NotNull(message = "수신자 프로필 ID는 필수입니다")
    private Long receiverProfileId;

    @NotBlank(message = "내용은 필수입니다")
    private String content;
}