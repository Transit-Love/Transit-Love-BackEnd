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
            sendErrorToChatRoom(chatRoomId, message.getFromProfileId(), "이미 진행 중인 통화가 있습니다.");
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

        // 채팅방 전체에 통화 요청 브로드캐스트
        broadcastToChatRoom(chatRoomId, message);

        log.info("음성 통화 요청 브로드캐스트 - 채팅방: {}, 발신자: {}, 수신자: {}",
                chatRoomId, message.getFromProfileId(), message.getToProfileId());
    }

    /**
     * 음성 통화 수락 처리
     */
    public void handleVoiceCallAccept(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.get(chatRoomId);

        if (session == null) {
            sendErrorToChatRoom(chatRoomId, message.getFromProfileId(), "통화 요청을 찾을 수 없습니다.");
            return;
        }

        session.setAccepted(true);

        // 채팅방 전체에 수락 알림 브로드캐스트
        broadcastToChatRoom(chatRoomId, message);

        log.info("음성 통화 수락 브로드캐스트 - 채팅방: {}, 수락자: {}", chatRoomId, message.getFromProfileId());
    }

    /**
     * 음성 통화 거절 처리
     */
    public void handleVoiceCallReject(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.remove(chatRoomId);

        if (session != null) {
            // 채팅방 전체에 거절 알림 브로드캐스트
            broadcastToChatRoom(chatRoomId, message);
            log.info("음성 통화 거절 브로드캐스트 - 채팅방: {}", chatRoomId);
        }
    }

    /**
     * 음성 통화 종료 처리
     */
    public void handleVoiceCallEnd(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.remove(chatRoomId);

        if (session != null) {
            // 채팅방 전체에 종료 알림 브로드캐스트
            broadcastToChatRoom(chatRoomId, message);
            log.info("음성 통화 종료 브로드캐스트 - 채팅방: {}, 종료자: {}", chatRoomId, message.getFromProfileId());
        }
    }

    /**
     * 음성 통화 취소 처리 (요청자가 취소)
     */
    public void handleVoiceCallCancel(VoiceSignalMessage message) {
        Long chatRoomId = message.getChatRoomId();
        VoiceCallSession session = activeSessions.remove(chatRoomId);

        if (session != null) {
            // 채팅방 전체에 취소 알림 브로드캐스트
            broadcastToChatRoom(chatRoomId, message);
            log.info("음성 통화 취소 브로드캐스트 - 채팅방: {}", chatRoomId);
        }
    }

    /**
     * WebRTC Offer 처리
     */
    public void handleOffer(VoiceSignalMessage message) {
        // 채팅방 전체에 Offer 브로드캐스트
        broadcastToChatRoom(message.getChatRoomId(), message);
        log.info("WebRTC Offer 브로드캐스트 - 채팅방: {}", message.getChatRoomId());
    }

    /**
     * WebRTC Answer 처리
     */
    public void handleAnswer(VoiceSignalMessage message) {
        // 채팅방 전체에 Answer 브로드캐스트
        broadcastToChatRoom(message.getChatRoomId(), message);
        log.info("WebRTC Answer 브로드캐스트 - 채팅방: {}", message.getChatRoomId());
    }

    /**
     * ICE Candidate 처리
     */
    public void handleIceCandidate(VoiceSignalMessage message) {
        // 채팅방 전체에 ICE Candidate 브로드캐스트
        broadcastToChatRoom(message.getChatRoomId(), message);
        log.debug("ICE Candidate 브로드캐스트 - 채팅방: {}", message.getChatRoomId());
    }

    /**
     * 채팅방 전체에 메시지 브로드캐스트
     * /topic/voice.{chatRoomId}로 전송하면 해당 채팅방을 구독한 모든 사용자가 수신
     */
    private void broadcastToChatRoom(Long chatRoomId, VoiceSignalMessage message) {
        String destination = "/topic/voice." + chatRoomId;
        messagingTemplate.convertAndSend(destination, message);
        log.debug("메시지 브로드캐스트 - 목적지: {}, 타입: {}", destination, message.getType());
    }

    /**
     * 특정 사용자에게 메시지 전송 (개별 큐 사용)
     * 주로 에러 메시지나 개인 알림용
     */
    private void sendToUser(Long profileId, VoiceSignalMessage message) {
        String destination = "/queue/voice." + profileId;
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * 채팅방에 에러 메시지 브로드캐스트
     */
    private void sendErrorToChatRoom(Long chatRoomId, Long targetProfileId, String errorMessage) {
        VoiceSignalMessage errorMsg = VoiceSignalMessage.builder()
                .type(VoiceSignalType.ERROR)
                .chatRoomId(chatRoomId)
                .toProfileId(targetProfileId)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
        broadcastToChatRoom(chatRoomId, errorMsg);
        log.warn("음성 채팅 에러 브로드캐스트 - 채팅방: {}, 대상 프로필: {}, 메시지: {}",
                chatRoomId, targetProfileId, errorMessage);
    }

    /**
     * 에러 메시지 전송 (개별 사용자)
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
            // 채팅방 전체에 강제 종료 알림 브로드캐스트
            Long partnerId = session.getCallerId().equals(profileId) ?
                    session.getReceiverId() : session.getCallerId();

            VoiceSignalMessage endMessage = VoiceSignalMessage.builder()
                    .type(VoiceSignalType.VOICE_CALL_END)
                    .chatRoomId(chatRoomId)
                    .fromProfileId(profileId)
                    .toProfileId(partnerId)
                    .senderId(profileId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            broadcastToChatRoom(chatRoomId, endMessage);
            log.info("음성 통화 강제 종료 브로드캐스트 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);
        }
    }

    /**
     * 통화 중 상태 변경 브로드캐스팅
     * 음소거, 스피커 상태 등의 변경을 채팅방 전체에 알림
     */
    public void broadcastStatusChange(VoiceSignalMessage message) {
        broadcastToChatRoom(message.getChatRoomId(), message);
        log.debug("통화 상태 변경 브로드캐스트 - 채팅방: {}, 타입: {}",
                message.getChatRoomId(), message.getType());
    }

    /**
     * 통화 연결 성공 알림
     * WebRTC 연결이 완료되면 호출
     */
    public void broadcastCallConnected(Long chatRoomId, Long callerProfileId, Long receiverProfileId) {
        VoiceCallSession session = activeSessions.get(chatRoomId);
        if (session != null && session.isAccepted()) {
            VoiceSignalMessage message = VoiceSignalMessage.builder()
                    .type(VoiceSignalType.VOICE_CALL_ACCEPT)
                    .chatRoomId(chatRoomId)
                    .fromProfileId(receiverProfileId)
                    .toProfileId(callerProfileId)
                    .senderId(receiverProfileId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            broadcastToChatRoom(chatRoomId, message);
            log.info("통화 연결 성공 브로드캐스트 - 채팅방: {}", chatRoomId);
        }
    }

    /**
     * 통화 상태 조회 및 브로드캐스트
     * 클라이언트가 재연결 시 현재 통화 상태를 확인하고 동기화
     */
    public void syncCallStatus(Long chatRoomId, Long profileId) {
        VoiceCallSession session = activeSessions.get(chatRoomId);
        if (session != null) {
            // 현재 진행 중인 통화 정보를 해당 사용자에게 전송
            VoiceSignalMessage statusMessage = VoiceSignalMessage.builder()
                    .type(VoiceSignalType.VOICE_CALL_REQUEST)
                    .chatRoomId(chatRoomId)
                    .fromProfileId(session.getCallerId())
                    .toProfileId(session.getReceiverId())
                    .senderId(0L) // 시스템 메시지
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 개별 사용자에게만 전송
            sendToUser(profileId, statusMessage);
            log.info("통화 상태 동기화 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);
        }
    }

    /**
     * 모든 활성 세션 조회
     */
    public Map<Long, VoiceCallSession> getAllActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    /**
     * 특정 프로필의 활성 통화 조회
     */
    public VoiceCallSession getActiveSessionByProfile(Long profileId) {
        return activeSessions.values().stream()
                .filter(session ->
                        session.getCallerId().equals(profileId) ||
                        session.getReceiverId().equals(profileId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 채팅방에 일반 알림 브로드캐스트
     * 통화 관련 일반 알림용 (연결 품질, 네트워크 상태 등)
     */
    public void broadcastNotification(Long chatRoomId, String notificationType, String message) {
        VoiceSignalMessage notification = VoiceSignalMessage.builder()
                .type(VoiceSignalType.ERROR) // 일반 알림도 ERROR 타입 사용
                .chatRoomId(chatRoomId)
                .errorMessage(notificationType + ": " + message)
                .timestamp(System.currentTimeMillis())
                .build();

        broadcastToChatRoom(chatRoomId, notification);
        log.info("일반 알림 브로드캐스트 - 채팅방: {}, 타입: {}, 메시지: {}",
                chatRoomId, notificationType, message);
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

