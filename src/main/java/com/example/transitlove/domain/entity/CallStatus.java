package com.example.transitlove.domain.entity;

/**
 * 통화 상태 Enum
 */
public enum CallStatus {
    COMPLETED("정상 종료"),
    REJECTED("거절됨"),
    FAILED("연결 실패"),
    MISSED("부재중");

    private final String description;

    CallStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

