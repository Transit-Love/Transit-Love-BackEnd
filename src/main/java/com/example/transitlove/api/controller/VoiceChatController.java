package com.example.transitlove.api.controller;

import com.example.transitlove.domain.vo.VoiceRoomResponse;
import com.example.transitlove.domain.vo.VoiceSignalMessage;
import com.example.transitlove.service.VoiceChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * 음성 채팅 컨트롤러
 * WebSocket(STOMP)를 통한 음성 채팅 시그널링 및 REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class VoiceChatController {

    private final VoiceChatService voiceChatService;

    /**
     * 음성 채팅방 정보 조회 (REST API)
     * GET /api/voice/room/{chatRoomId}
     */
    @GetMapping("/api/voice/room/{chatRoomId}")
    public ResponseEntity<VoiceRoomResponse> getVoiceRoom(
            @PathVariable Long chatRoomId,
            Authentication authentication) {

        Long profileId = Long.parseLong(authentication.getName());
        VoiceRoomResponse response = voiceChatService.getVoiceRoom(chatRoomId, profileId);

        return ResponseEntity.ok(response);
    }

    /**
     * 음성 채팅 시그널 메시지 처리 (WebSocket)
     * STOMP destination: /app/voice/signal
     *
     * 클라이언트는 이 엔드포인트로 모든 음성 채팅 관련 시그널을 전송
     */
    @MessageMapping("/voice/signal")
    public void handleVoiceSignal(
            @Payload VoiceSignalMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            // 사용자 인증 정보 가져오기
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                log.error("인증되지 않은 사용자의 음성 시그널 시도");
                return;
            }

            // 타임스탬프 설정
            if (message.getTimestamp() == null) {
                message.setTimestamp(System.currentTimeMillis());
            }

            // 시그널 타입에 따라 처리
            switch (message.getType()) {
                case VOICE_CALL_REQUEST:
                    voiceChatService.handleVoiceCallRequest(message);
                    break;

                case VOICE_CALL_ACCEPT:
                    voiceChatService.handleVoiceCallAccept(message);
                    break;

                case VOICE_CALL_REJECT:
                    voiceChatService.handleVoiceCallReject(message);
                    break;

                case VOICE_CALL_END:
                    voiceChatService.handleVoiceCallEnd(message);
                    break;

                case VOICE_CALL_CANCEL:
                    voiceChatService.handleVoiceCallCancel(message);
                    break;

                case OFFER:
                    voiceChatService.handleOffer(message);
                    break;

                case ANSWER:
                    voiceChatService.handleAnswer(message);
                    break;

                case ICE_CANDIDATE:
                    voiceChatService.handleIceCandidate(message);
                    break;

                default:
                    log.warn("알 수 없는 시그널 타입: {}", message.getType());
            }

        } catch (Exception e) {
            log.error("음성 시그널 처리 중 오류 발생", e);
        }
    }

    /**
     * 음성 통화 강제 종료 (REST API)
     * POST /api/voice/room/{chatRoomId}/end
     */
    @PostMapping("/api/voice/room/{chatRoomId}/end")
    public ResponseEntity<Void> endVoiceCall(
            @PathVariable Long chatRoomId,
            Authentication authentication) {

        Long profileId = Long.parseLong(authentication.getName());
        voiceChatService.forceEndSession(chatRoomId, profileId);

        return ResponseEntity.ok().build();
    }

    /**
     * 활성 세션 상태 조회 (REST API)
     * GET /api/voice/room/{chatRoomId}/status
     */
    @GetMapping("/api/voice/room/{chatRoomId}/status")
    public ResponseEntity<?> getSessionStatus(
            @PathVariable Long chatRoomId,
            Authentication authentication) {

        VoiceChatService.VoiceCallSession session = voiceChatService.getActiveSession(chatRoomId);

        if (session == null) {
            return ResponseEntity.ok().body(new SessionStatusResponse(false, null));
        }

        return ResponseEntity.ok().body(
                new SessionStatusResponse(true, session)
        );
    }

    /**
     * 세션 상태 응답 DTO
     */
    private record SessionStatusResponse(
            boolean active,
            VoiceChatService.VoiceCallSession session
    ) {}
}

