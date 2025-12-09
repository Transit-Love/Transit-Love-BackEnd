package com.example.transitlove.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MBTI {
    ISTJ("ISTJ"),
    ISFJ("ISFJ"),
    INFJ("INFJ"),
    INTJ("INTJ"),
    ISTP("ISTP"),
    ISFP("ISFP"),
    INFP("INFP"),
    INTP("INTP"),
    ESTP("ESTP"),
    ESFP("ESFP"),
    ENFP("ENFP"),
    ENTP("ENTP"),
    ESTJ("ESTJ"),
    ESFJ("ESFJ"),
    ENFJ("ENFJ"),
    ENTJ("ENTJ");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MBTI from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String upperValue = value.trim().toUpperCase();
        for (MBTI mbti : MBTI.values()) {
            if (mbti.value.equals(upperValue)) {
                return mbti;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 MBTI 타입입니다: " + value +
            ". 허용되는 값: ISTJ, ISFJ, INFJ, INTJ, ISTP, ISFP, INFP, INTP, " +
            "ESTP, ESFP, ENFP, ENTP, ESTJ, ESFJ, ENFJ, ENTJ");
    }

    @Override
    public String toString() {
        return this.value;
    }
}

