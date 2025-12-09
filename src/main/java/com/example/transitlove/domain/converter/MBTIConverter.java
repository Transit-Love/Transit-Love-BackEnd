package com.example.transitlove.domain.converter;

import com.example.transitlove.domain.enums.MBTI;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * MBTI ENUM을 데이터베이스에 저장하고 불러올 때 사용하는 컨버터
 * 잘못된 MBTI 값이 있을 경우 자동으로 수정하거나 null로 처리
 */
@Slf4j
@Converter(autoApply = true)
public class MBTIConverter implements AttributeConverter<MBTI, String> {

    @Override
    public String convertToDatabaseColumn(MBTI attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public MBTI convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            // 일반적인 변환 시도
            return MBTI.from(dbData);
        } catch (IllegalArgumentException e) {
            // 잘못된 MBTI 값 처리
            log.warn("잘못된 MBTI 값 발견: '{}'. 자동 수정 시도...", dbData);

            // 일반적인 오타 패턴 수정
            String corrected = correctCommonTypos(dbData);
            if (corrected != null) {
                log.info("MBTI 값 자동 수정: '{}' -> '{}'", dbData, corrected);
                try {
                    return MBTI.from(corrected);
                } catch (IllegalArgumentException ex) {
                    // 수정 후에도 실패하면 null 반환
                }
            }

            log.error("MBTI 값을 수정할 수 없습니다: '{}'. null로 처리합니다.", dbData);
            return null;
        }
    }

    /**
     * 일반적인 MBTI 오타를 수정
     */
    private String correctCommonTypos(String value) {
        if (value == null) {
            return null;
        }

        String upper = value.trim().toUpperCase();

        // 일반적인 오타 패턴
        switch (upper) {
            case "ENFO": return "ENFP";  // O -> P
            case "INJO": return "INFP";  // O -> P
            case "ISTO": return "ISTP";  // O -> P
            case "ESFO": return "ESFP";  // O -> P
            // 필요한 경우 더 추가 가능
            default:
                // 길이가 4가 아니면 null
                if (upper.length() != 4) {
                    return null;
                }
                // 그 외의 경우는 수정하지 않음
                return upper;
        }
    }
}

