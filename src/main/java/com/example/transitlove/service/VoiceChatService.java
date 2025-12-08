package com.example.transitlove.service;

import com.example.transitlove.domain.entity.ChatRoom;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.ChatRoomRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.vo.VoiceRoomResponse;
import com.example.transitlove.domain.vo.VoiceSignalMessage;
import com.example.transitlove.domain.vo.VoiceSignalType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 음성 채팅 서비스
 * WebRTC signaling 및 음성 통화 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ProfileRepository profileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 활성 음성 통화 세션 관리
     * Key: chatRoomId, Value: VoiceCallSession
     */
    private final Map<Long, VoiceCallSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * 음성 채팅방 정보 조회
     */
    @Transactional(readOnly = true)
    public VoiceRoomResponse getVoiceRoom(Long chatRoomId, Long profileId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + chatRoomId));

        // 권한 확인
        Profile myProfile = chatRoom.getProfile1().getId().equals(profileId) ?
                chatRoom.getProfile1() : chatRoom.getProfile2();
        Profile partnerProfile = chatRoom.getProfile1().getId().equals(profileId) ?
                chatRoom.getProfile2() : chatRoom.getProfile1();

        if (myProfile == null || !myProfile.getId().equals(profileId)) {
            throw new IllegalArgumentException("해당 채팅방에 접근 권한이 없습니다.");
        }

        log.info("음성 채팅방 정보 조회 - 채팅방 ID: {}, 프로필 ID: {}", chatRoomId, profileId);

        return VoiceRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .myProfileId(myProfile.getId())
                .partnerProfileId(partnerProfile.getId())
                .partnerNickname(partnerProfile.getNickname())
                .voiceEnabled(true)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    /**
     * 음성 통화 요청 처리
     */
    public void handleVoiceCallRequest(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();

        // 이미 진행 중인 통화가 있는지 확인
        if (activeSessions.containsKey(chatRoomId)) {
            sendError(message.getFromProfileId(), chatRoomId, "이미 진행 중인 통화가 있습니다.");
            return;
        }

        // 세션 생성
        VoiceCallSession session = new VoiceCallSession(
                chatRoomId,
                message.getFromProfileId(),
                message.getToProfileId(),
                LocalDateTime.now()
        );
        activeSessions.put(chatRoomId, session);

        // 상대방에게 통화 요청 전송
        sendToUser(message.getToProfileId(), message);

        log.info("음성 통화 요청 - 채팅방: {}, 발신자: {}, 수신자: {}",
                chatRoomId, message.getFromProfileId(), message.getToProfileId());
    }

    /**
     * 음성 통화 수락 처리
     */
    public void handleVoiceCallAccept(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.get(chatRoomId);

        if (session == null) {
            sendError(message.getFromProfileId(), chatRoomId, "통화 요청을 찾을 수 없습니다.");
            return;
        }

        session.setAccepted(true);

        // 발신자에게 수락 알림
        sendToUser(session.getCallerId(), message);

        log.info("음성 통화 수락 - 채팅방: {}, 수락자: {}", chatRoomId, message.getFromProfileId());
    }

    /**
     * 음성 통화 거절 처리
     */
    public void handleVoiceCallReject(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.remove(chatRoomId);

        if (session != null) {
            // 발신자에게 거절 알림
            sendToUser(session.getCallerId(), message);
            log.info("음성 통화 거절 - 채팅방: {}", chatRoomId);
        }
    }

    /**
     * 음성 통화 종료 처리
     */
    public void handleVoiceCallEnd(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.remove(chatRoomId);

        if (session != null) {
            // 상대방에게 종료 알림
            Long partnerId = session.getCallerId().equals(message.getFromProfileId()) ?
                    session.getReceiverId() : session.getCallerId();
            sendToUser(partnerId, message);

            log.info("음성 통화 종료 - 채팅방: {}, 종료자: {}", chatRoomId, message.getFromProfileId());
        }
    }

    /**
     * 음성 통화 취소 처리 (요청자가 취소)
     */
    public void handleVoiceCallCancel(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.remove(chatRoomId);

        if (session != null) {
            // 수신자에게 취소 알림
            sendToUser(session.getReceiverId(), message);
            log.info("음성 통화 취소 - 채팅방: {}", chatRoomId);
        }
    }

    /**
     * WebRTC Offer 처리
     */
    public void handleOffer(VoiceSignalMessage message) {
        // 상대방에게 Offer 전달
        sendToUser(message.getToProfileId(), message);
        log.info("WebRTC Offer 전달 - 채팅방: {}", message.getChatRoomId());
    }

    /**
     * WebRTC Answer 처리
     */
    public void handleAnswer(VoiceSignalMessage message) {
        // 상대방에게 Answer 전달
        sendToUser(message.getToProfileId(), message);
        log.info("WebRTC Answer 전달 - 채팅방: {}", message.getChatRoomId());
    }

    /**
     * ICE Candidate 처리
     */
    public void handleIceCandidate(VoiceSignalMessage message) {
        // 상대방에게 ICE Candidate 전달
        sendToUser(message.getToProfileId(), message);
        log.debug("ICE Candidate 전달 - 채팅방: {}", message.getChatRoomId());
    }

    /**
     * 특정 사용자에게 메시지 전송
     */
    private void sendToUser(Long profileId, VoiceSignalMessage message) {
        String destination = "/queue/voice." + profileId;
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * 에러 메시지 전송
     */
    private void sendError(Long profileId, Long chatRoomId, String errorMessage) {
        VoiceSignalMessage errorMsg = VoiceSignalMessage.builder()
                .type(VoiceSignalType.ERROR)
                .chatRoomId(chatRoomId)
                .toProfileId(profileId)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
        sendToUser(profileId, errorMsg);
        log.warn("음성 채팅 에러 - 프로필: {}, 메시지: {}", profileId, errorMessage);
    }

    /**
     * 활성 세션 조회
     */
    public VoiceCallSession getActiveSession(Long chatRoomId) {
        return activeSessions.get(chatRoomId);
    }

    /**
     * 세션 강제 종료 (연결 끊김 등)
     */
    public void forceEndSession(Long chatRoomId, Long profileId) {
        VoiceCallSession session = activeSessions.remove(chatRoomId);
        if (session != null) {
            // 상대방에게 강제 종료 알림
            Long partnerId = session.getCallerId().equals(profileId) ?
                    session.getReceiverId() : session.getCallerId();

            VoiceSignalMessage endMessage = VoiceSignalMessage.builder()
                    .type(VoiceSignalType.VOICE_CALL_END)
                    .chatRoomId(chatRoomId)
                    .fromProfileId(profileId)
                    .toProfileId(partnerId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendToUser(partnerId, endMessage);
            log.info("음성 통화 강제 종료 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);
        }
    }

    /**
     * 음성 통화 세션 정보
     */
    @Getter
    @RequiredArgsConstructor
    public static class VoiceCallSession {
        private final Long chatRoomId;
        private final Long callerId;      // 발신자
        private final Long receiverId;    // 수신자
        private final LocalDateTime startTime;
        private boolean accepted = false;  // 수락 여부

        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }
    }
}

