package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 마지막 선택 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChoiceResponse {

    private Long matchId;
    private Long myProfileId;
    private Long partnerProfileId;
    private String partnerNickname;
    private String partnerMbti;
    private Boolean isChanged; // 매칭이 변경되었는지 여부
    private LocalDateTime finalizedAt;

    public static FinalChoiceResponse from(Match match, Long myProfileId, Boolean isChanged) {
        Profile partner = match.getOtherProfile(myProfileId);

        return FinalChoiceResponse.builder()
                .matchId(match.getId())
                .myProfileId(myProfileId)
                .partnerProfileId(partner.getId())
                .partnerNickname(partner.getNickname())
                .partnerMbti(partner.getMbti())
                .isChanged(isChanged)
                .finalizedAt(LocalDateTime.now())
                .build();
    }
}

