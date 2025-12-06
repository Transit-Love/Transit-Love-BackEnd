package com.example.transitlove.api.dto;

import com.example.transitlove.domain.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String nickname;
    private String mbti;
    private List<KeywordDto> keywords;
    private List<BalanceGameAnswerDto> balanceGameAnswers;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class KeywordDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BalanceGameAnswerDto {
        private Long balanceGameId;
        private String question;
        private String option1;
        private String option2;
        private Integer selectedOption;
    }

    public static ProfileResponse from(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .nickname(profile.getNickname())
                .mbti(profile.getMbti())
                .keywords(profile.getProfileKeywords().stream()
                        .map(pk -> KeywordDto.builder()
                                .id(pk.getKeyword().getId())
                                .name(pk.getKeyword().getName())
                                .build())
                        .collect(Collectors.toList()))
                .balanceGameAnswers(profile.getBalanceGameAnswers().stream()
                        .map(bga -> BalanceGameAnswerDto.builder()
                                .balanceGameId(bga.getBalanceGame().getId())
                                .question(bga.getBalanceGame().getQuestion())
                                .option1(bga.getBalanceGame().getOption1())
                                .option2(bga.getBalanceGame().getOption2())
                                .selectedOption(bga.getSelectedOption())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(profile.getCreatedAt())
                .build();
    }
}