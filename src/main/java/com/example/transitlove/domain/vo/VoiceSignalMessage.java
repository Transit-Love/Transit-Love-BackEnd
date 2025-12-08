package com.example.transitlove.domain.vo;

import lombok.*;

/**
 * 음성 채팅 시그널 메시지
 * WebRTC signaling을 위한 메시지 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceSignalMessage {

    /**
     * 시그널 타입
     */
    private VoiceSignalType type;

    /**
     * 채팅방 ID
     */
    private Long chatRoomId;

    /**
     * 발신자 프로필 ID
     */
    private Long fromProfileId;

    /**
     * 수신자 프로필 ID
     */
    private Long toProfileId;

    /**
     * 메시지 발신자 ID (필터링용)
     * 클라이언트에서 자신이 보낸 메시지를 제외하는데 사용
     */
    private Long senderId;

    /**
     * WebRTC SDP (Session Description Protocol)
     * OFFER, ANSWER 타입에서 사용
     */
    private String sdp;

    /**
     * ICE Candidate 정보
     * ICE_CANDIDATE 타입에서 사용
     */
    private IceCandidate iceCandidate;

    /**
     * 에러 메시지
     */
    private String errorMessage;

    /**
     * 타임스탬프
     */
    private Long timestamp;

    /**
     * 음소거 상태
     */
    private Boolean isMuted;

    /**
     * 스피커 상태
     */
    private Boolean isSpeakerOn;

    /**
     * ICE Candidate DTO
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IceCandidate {
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;
    }
}

