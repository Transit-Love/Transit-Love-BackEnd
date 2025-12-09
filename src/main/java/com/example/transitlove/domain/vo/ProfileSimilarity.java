package com.example.transitlove.domain.vo;

import com.example.transitlove.domain.entity.BalanceGameAnswer;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.enums.MBTI;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프로필 유사도 계산 Value Object
 * DDD - Value Object
 */
@Value
public class ProfileSimilarity {

    Profile profile1;
    Profile profile2;
    double mbtiScore;
    double balanceGameScore;
    double totalScore;

    private ProfileSimilarity(Profile profile1, Profile profile2, double mbtiScore, double balanceGameScore) {
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.mbtiScore = mbtiScore;
        this.balanceGameScore = balanceGameScore;
        // 가중치: MBTI 30%, 밸런스 게임 70%
        this.totalScore = (mbtiScore * 0.3) + (balanceGameScore * 0.7);
    }

    /**
     * 두 프로필의 유사도 계산
     */
    public static ProfileSimilarity calculate(Profile profile1, Profile profile2) {
        double mbtiScore = calculateMBTIScore(profile1.getMbti(), profile2.getMbti());
        double balanceGameScore = calculateBalanceGameScore(
                profile1.getBalanceGameAnswers(),
                profile2.getBalanceGameAnswers()
        );

        return new ProfileSimilarity(profile1, profile2, mbtiScore, balanceGameScore);
    }

    /**
     * MBTI 유사도 계산
     * - 완전 일치: 1.0
     * - 3글자 일치: 0.75
     * - 2글자 일치: 0.5
     * - 1글자 일치: 0.25
     * - 불일치: 0.0
     */
    private static double calculateMBTIScore(MBTI mbti1, MBTI mbti2) {
        if (mbti1 == null || mbti2 == null) {
            return 0.0;
        }

        String mbti1Str = mbti1.getValue();
        String mbti2Str = mbti2.getValue();

        int matchCount = 0;
        for (int i = 0; i < 4; i++) {
            if (mbti1Str.charAt(i) == mbti2Str.charAt(i)) {
                matchCount++;
            }
        }

        return matchCount / 4.0;
    }

    /**
     * 밸런스 게임 답변 유사도 계산
     * 같은 선택을 한 비율로 계산
     */
    private static double calculateBalanceGameScore(
            List<BalanceGameAnswer> answers1,
            List<BalanceGameAnswer> answers2) {

        if (answers1.isEmpty() || answers2.isEmpty()) {
            return 0.0;
        }

        // 밸런스 게임 ID를 키로 하는 맵 생성
        Map<Long, Integer> answerMap1 = answers1.stream()
                .collect(Collectors.toMap(
                        a -> a.getBalanceGame().getId(),
                        BalanceGameAnswer::getSelectedOption
                ));

        Map<Long, Integer> answerMap2 = answers2.stream()
                .collect(Collectors.toMap(
                        a -> a.getBalanceGame().getId(),
                        BalanceGameAnswer::getSelectedOption
                ));

        // 공통 질문 찾기
        int commonQuestions = 0;
        int matchingAnswers = 0;

        for (Map.Entry<Long, Integer> entry : answerMap1.entrySet()) {
            Long gameId = entry.getKey();
            if (answerMap2.containsKey(gameId)) {
                commonQuestions++;
                if (entry.getValue().equals(answerMap2.get(gameId))) {
                    matchingAnswers++;
                }
            }
        }

        if (commonQuestions == 0) {
            return 0.0;
        }

        return (double) matchingAnswers / commonQuestions;
    }

    /**
     * 유사도 퍼센트 표시
     */
    public int getTotalScorePercentage() {
        return (int) (totalScore * 100);
    }
}

