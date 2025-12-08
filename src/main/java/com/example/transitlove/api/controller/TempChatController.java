package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.TempChatMessageResponse;
import com.example.transitlove.api.dto.TempChatResponse;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.TempChat;
import com.example.transitlove.domain.entity.TempChatMessage;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.TempChatMessageRepository;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.service.TempChatService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 임시 채팅 컨트롤러 (2시간 제한 채팅)
 * DDD - Presentation Layer
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TempChatController {

    private final TempChatService tempChatService;
    private final TempChatMessageRepository tempChatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    // ===== WebSocket Handlers =====

    /**
     * 임시 채팅 메시지 전송
     * /app/tempChat.send
     */
    @MessageMapping("/tempChat.send")
    public void sendTempChatMessage(@Payload TempChatMessageRequest request, Principal principal) {
        try {
            log.debug("임시 채팅 메시지 수신 - 채팅: {}, 사용자: {}", request.getTempChatId(), principal.getName());

            Long profileId = getProfileIdFromPrincipal(principal);

            TempChatMessage message = tempChatService.sendMessage(
                    profileId,
                    request.getTempChatId(),
                    request.getContent(),
                    request.getType() != null ? TempChatMessage.MessageType.valueOf(request.getType()) : null
            );

            TempChatMessageResponse response = TempChatMessageResponse.from(message);

            // 임시 채팅방에 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/tempChat/" + request.getTempChatId(),
                    response
            );

            log.info("임시 채팅 메시지 브로드캐스트 완료 - ID: {}", message.getId());
        } catch (Exception e) {
            log.error("임시 채팅 메시지 전송 실패", e);
            sendErrorToUser(principal, "메시지 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 임시 채팅 메시지 읽음 처리
     * /app/tempChat.read
     */
    @MessageMapping("/tempChat.read")
    public void markTempChatAsRead(@Payload Long tempChatId, Principal principal) {
        try {
            log.debug("임시 채팅 읽음 처리 - 채팅: {}, 사용자: {}", tempChatId, principal.getName());

            Long profileId = getProfileIdFromPrincipal(principal);
            tempChatService.markMessagesAsRead(profileId, tempChatId);

            messagingTemplate.convertAndSend(
                    "/topic/tempChat/" + tempChatId + "/read",
                    profileId
            );

            log.info("임시 채팅 읽음 처리 완료 - 채팅: {}", tempChatId);
        } catch (Exception e) {
            log.error("읽음 처리 실패", e);
            sendErrorToUser(principal, "읽음 처리 실패: " + e.getMessage());
        }
    }

    // ===== REST API Endpoints =====

    /**
     * 임시 채팅 생성
     * POST /api/temp-chat?targetProfileId={id}
     */
    @PostMapping("/api/temp-chat")
    public ResponseEntity<TempChatResponse> createTempChat(
            @RequestParam Long targetProfileId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("임시 채팅 생성 요청 - 대상: {}", targetProfileId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        TempChat tempChat = tempChatService.createTempChat(profileId, targetProfileId);

        TempChatResponse response = TempChatResponse.from(tempChat, profileId, 0L, null);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 임시 채팅 목록 조회
     * GET /api/temp-chat
     */
    @GetMapping("/api/temp-chat")
    public ResponseEntity<List<TempChatResponse>> getMyTempChats(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("임시 채팅 목록 조회");

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        List<TempChat> tempChats = tempChatService.getMyTempChats(profileId);

        List<TempChatResponse> responses = tempChats.stream()
                .map(tc -> {
                    Long unreadCount = tempChatMessageRepository.countUnreadMessagesByTempChatIdAndNotSender(
                            tc.getId(), profileId);
                    TempChatMessage lastMessage = tempChatMessageRepository.findLatestMessageByTempChatId(tc.getId());
                    return TempChatResponse.from(tc, profileId, unreadCount, lastMessage);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 임시 채팅 메시지 목록 조회
     * GET /api/temp-chat/{tempChatId}/messages
     */
    @GetMapping("/api/temp-chat/{tempChatId}/messages")
    public ResponseEntity<List<TempChatMessageResponse>> getTempChatMessages(
            @PathVariable Long tempChatId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("임시 채팅 메시지 조회 - 채팅: {}", tempChatId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        List<TempChatMessage> messages = tempChatService.getMessages(profileId, tempChatId);

        List<TempChatMessageResponse> responses = messages.stream()
                .map(TempChatMessageResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 임시 채팅 읽음 처리
     * PUT /api/temp-chat/{tempChatId}/read
     */
    @PutMapping("/api/temp-chat/{tempChatId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Long tempChatId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("임시 채팅 읽음 처리 - 채팅: {}", tempChatId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        tempChatService.markMessagesAsRead(profileId, tempChatId);

        return ResponseEntity.ok("읽음 처리되었습니다.");
    }

    /**
     * 임시 채팅 종료
     * DELETE /api/temp-chat/{tempChatId}
     */
    @DeleteMapping("/api/temp-chat/{tempChatId}")
    public ResponseEntity<String> endTempChat(
            @PathVariable Long tempChatId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("임시 채팅 종료 요청 - 채팅: {}", tempChatId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        tempChatService.endTempChat(profileId, tempChatId);

        return ResponseEntity.ok("임시 채팅이 종료되었습니다.");
    }

    // ===== Helper Classes =====

    @Getter
    @Setter
    public static class TempChatMessageRequest {
        private Long tempChatId;
        private String content;
        private String type;
    }

    // ===== Helper Methods =====

    private Long getProfileIdFromOAuth2User(CustomOAuth2User oauth2User) {
        // JWT 필터에서 CustomOAuth2User를 생성하므로 null이 아님
        if (oauth2User == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }

        User user = userRepository.findBySchoolEmail(oauth2User.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        return profile.getId();
    }

    private Long getProfileIdFromPrincipal(Principal principal) {
        User user = userRepository.findBySchoolEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        return profile.getId();
    }

    private void sendErrorToUser(Principal principal, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                errorMessage
        );
    }
}

