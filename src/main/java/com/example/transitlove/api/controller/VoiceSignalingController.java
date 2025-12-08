package com.example.transitlove.api.controller;

import com.example.transitlove.domain.vo.VoiceSignalMessage;
import com.example.transitlove.domain.vo.VoiceSignalType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 음성 채팅 시그널링 컨트롤러 (명세서 기준)
 *
 * WebSocket STOMP를 통한 WebRTC 시그널링
 * - 메시지 경로: /app/voice/{chatRoomId}
 * - 브로드캐스트: /topic/voice.{chatRoomId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class VoiceSignalingController {

    /**
     * 음성 채팅 시그널 처리
     *
     * 클라이언트 -> 서버: /app/voice/{chatRoomId}
     * 서버 -> 클라이언트: /topic/voice.{chatRoomId}
     *
     * 같은 채팅방에 구독한 모든 사용자에게 브로드캐스트됨
     * 클라이언트는 senderId를 확인하여 자신의 메시지를 필터링
     *
     * @param chatRoomId 채팅방 ID
     * @param message 시그널 메시지
     * @param headerAccessor WebSocket 헤더 정보
     * @return 브로드캐스트할 메시지
     */
    @MessageMapping("/voice/{chatRoomId}")
    @SendTo("/topic/voice.{chatRoomId}")
    public VoiceSignalMessage handleSignal(
            @DestinationVariable Long chatRoomId,
            VoiceSignalMessage message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            // 인증 확인
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                log.error("인증되지 않은 사용자의 음성 시그널 시도");
                return createErrorMessage(chatRoomId, "인증이 필요합니다.");
            }

            // 인증된 사용자 정보
            Authentication authentication = (Authentication) principal;
            Long authenticatedProfileId = Long.parseLong(authentication.getName());

            // 메시지 검증
            if (message.getChatRoomId() == null || !message.getChatRoomId().equals(chatRoomId)) {
                log.error("채팅방 ID 불일치 - 요청: {}, 메시지: {}", chatRoomId, message.getChatRoomId());
                return createErrorMessage(chatRoomId, "채팅방 ID가 일치하지 않습니다.");
            }

            // senderId 자동 설정 (보안상 클라이언트 값 무시하고 서버에서 설정)
            message.setSenderId(authenticatedProfileId);

            // fromProfileId도 자동 설정 (보안)
            message.setFromProfileId(authenticatedProfileId);

            // 타임스탬프 설정
            message.setTimestamp(System.currentTimeMillis());

            // 시그널 타입별 로깅
            logSignalMessage(message);

            // 메시지 브로드캐스트 (같은 채팅방의 모든 구독자에게 전송)
            return message;

        } catch (NumberFormatException e) {
            log.error("잘못된 프로필 ID 형식", e);
            return createErrorMessage(chatRoomId, "잘못된 사용자 정보입니다.");
        } catch (Exception e) {
            log.error("음성 시그널 처리 중 오류 발생", e);
            return createErrorMessage(chatRoomId, "시그널 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시그널 타입별 로깅
     */
    private void logSignalMessage(VoiceSignalMessage message) {
        String logMessage = String.format(
                "음성 시그널 처리 - 타입: %s, 채팅방: %d, 발신자: %d, 수신자: %d",
                message.getType(),
                message.getChatRoomId(),
                message.getFromProfileId(),
                message.getToProfileId()
        );

        switch (message.getType()) {
            case VOICE_CALL_REQUEST:
                log.info("{} - 통화 요청", logMessage);
                break;
            case VOICE_CALL_ACCEPT:
                log.info("{} - 통화 수락", logMessage);
                break;
            case VOICE_CALL_REJECT:
                log.info("{} - 통화 거절", logMessage);
                break;
            case VOICE_CALL_END:
                log.info("{} - 통화 종료", logMessage);
                break;
            case VOICE_CALL_CANCEL:
                log.info("{} - 통화 취소", logMessage);
                break;
            case OFFER:
                log.debug("{} - WebRTC Offer (SDP 포함)", logMessage);
                break;
            case ANSWER:
                log.debug("{} - WebRTC Answer (SDP 포함)", logMessage);
                break;
            case ICE_CANDIDATE:
                log.debug("{} - ICE Candidate", logMessage);
                break;
            case MUTE_STATUS_CHANGED:
                log.debug("{} - 음소거 상태 변경: {}", logMessage, message.getIsMuted());
                break;
            case SPEAKER_STATUS_CHANGED:
                log.debug("{} - 스피커 상태 변경: {}", logMessage, message.getIsSpeakerOn());
                break;
            default:
                log.warn("{} - 알 수 없는 시그널 타입", logMessage);
        }
    }

    /**
     * 에러 메시지 생성
     */
    private VoiceSignalMessage createErrorMessage(Long chatRoomId, String errorMessage) {
        return VoiceSignalMessage.builder()
                .type(VoiceSignalType.ERROR)
                .chatRoomId(chatRoomId)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

