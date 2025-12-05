package com.example.transitlove.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreateRequest {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
    private String nickname;

    @Size(min = 4, max = 4, message = "MBTI는 4자여야 합니다")
    private String mbti;

    @NotEmpty(message = "키워드를 최소 1개 이상 선택해야 합니다")
    private List<Long> keywordIds;

    @NotEmpty(message = "밸런스 게임 응답은 최소 1개 이상이어야 합니다")
    @Valid
    private List<BalanceGameAnswerDto> balanceGameAnswers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceGameAnswerDto {

        @NotNull(message = "밸런스 게임 ID는 필수입니다")
        private Long balanceGameId;

        @NotNull(message = "선택한 옵션은 필수입니다")
        private Integer selectedOption;
    }
}
