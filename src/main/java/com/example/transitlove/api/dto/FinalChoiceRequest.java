package com.example.transitlove.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마지막 선택 요청 DTO
 * 현재 매칭 상대를 유지하거나, 채팅했던 다른 사람으로 변경
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChoiceRequest {

    /**
     * 선택한 프로필 ID
     * - 현재 매칭 상대 ID: 그대로 유지
     * - 채팅했던 다른 프로필 ID: 매칭 변경
     */
    private Long selectedProfileId;
}
