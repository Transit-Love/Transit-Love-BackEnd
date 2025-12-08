package com.example.transitlove.domain.vo;

/**
 * 음성 채팅 시그널 타입
 * WebRTC signaling을 위한 메시지 타입 정의
 */
public enum VoiceSignalType {
    // WebRTC 연결 시그널
    OFFER,          // WebRTC Offer (발신자가 보냄)
    ANSWER,         // WebRTC Answer (수신자가 보냄)
    ICE_CANDIDATE,  // ICE candidate (양쪽이 교환)

    // 음성 채팅 상태 관리
    VOICE_CALL_REQUEST,  // 음성 통화 요청
    VOICE_CALL_ACCEPT,   // 음성 통화 수락
    VOICE_CALL_REJECT,   // 음성 통화 거절
    VOICE_CALL_END,      // 음성 통화 종료
    VOICE_CALL_CANCEL,   // 음성 통화 취소 (요청자가 취소)

    // 음소거 및 스피커 상태
    MUTE_STATUS_CHANGED,    // 음소거 상태 변경
    SPEAKER_STATUS_CHANGED, // 스피커 상태 변경

    // 기타
    ERROR                // 에러 메시지
}

