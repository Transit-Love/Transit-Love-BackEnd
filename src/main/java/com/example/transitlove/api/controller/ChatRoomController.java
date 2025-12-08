package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.*;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 채팅방 기반 채팅 API 컨트롤러
 * 매칭 여부와 관계없이 모든 사용자 간 채팅 지원
 */
@Slf4j
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    // ===== REST API Endpoints =====

    /**
     * 채팅방 생성 또는 조회
     * @param request 채팅방 요청 (상대방 프로필 ID)
     * @param oauth2User 인증된 사용자
     * @return 채팅방 정보
     */
    @PostMapping
    public ResponseEntity<ChatRoomResponse> getOrCreateChatRoom(
            @RequestBody ChatRoomRequest request,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 채팅방 생성/조회 - 상대방: {}", request.getTargetProfileId());

        Long myProfileId = getProfileIdFromOAuth2User(oauth2User);
        ChatRoomResponse chatRoom = chatRoomService.getOrCreateChatRoom(myProfileId, request.getTargetProfileId());

        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 내 채팅방 리스트 조회
     * @param oauth2User 인증된 사용자
     * @return 채팅방 리스트
     */
    @GetMapping
    public ResponseEntity<ChatRoomListResponse> getMyChatRooms(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 채팅방 리스트 조회");

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        ChatRoomListResponse chatRooms = chatRoomService.getMyChatRooms(profileId);

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방 메시지 조회
     * @param chatRoomId 채팅방 ID
     * @param oauth2User 인증된 사용자
     * @return 메시지 리스트
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatRoomMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 채팅방 메시지 조회 - 채팅방: {}", chatRoomId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        List<ChatMessageResponse> messages = chatRoomService.getChatRoomMessages(profileId, chatRoomId);

        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅방 메시지 읽음 처리
     * @param chatRoomId 채팅방 ID
     * @param oauth2User 인증된 사용자
     * @return 성공 메시지
     */
    @PutMapping("/{chatRoomId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 채팅방 메시지 읽음 처리 - 채팅방: {}", chatRoomId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        chatRoomService.markChatRoomMessagesAsRead(profileId, chatRoomId);

        return ResponseEntity.ok("메시지가 읽음 처리되었습니다.");
    }

    /**
     * 채팅방 비활성화
     * @param chatRoomId 채팅방 ID
     * @param oauth2User 인증된 사용자
     * @return 성공 메시지
     */
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<String> deactivateChatRoom(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 채팅방 비활성화 - 채팅방: {}", chatRoomId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        chatRoomService.deactivateChatRoom(profileId, chatRoomId);

        return ResponseEntity.ok("채팅방이 비활성화되었습니다.");
    }

    // ===== WebSocket Message Handlers =====

    /**
     * 채팅방 메시지 전송 핸들러
     * @param request 메시지 요청
     * @param principal 인증된 사용자 정보
     */
    @MessageMapping("/chatroom.send")
    public void sendMessage(@Payload ChatRoomMessageRequest request, Principal principal) {
        try {
            log.debug("WebSocket 메시지 수신 - 채팅방: {}, 사용자: {}", request.getChatRoomId(), principal.getName());

            Long profileId = getProfileIdFromPrincipal(principal);
            ChatMessageResponse response = chatRoomService.sendMessageToChatRoom(profileId, request);

            // 채팅방의 모든 참여자에게 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + request.getChatRoomId(),
                    response
            );

            log.info("메시지 브로드캐스트 완료 - 메시지 ID: {}", response.getId());
        } catch (Exception e) {
            log.error("메시지 전송 실패", e);
            sendErrorToUser(principal, "메시지 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 채팅방 메시지 읽음 처리 핸들러
     * @param chatRoomId 채팅방 ID
     * @param principal 인증된 사용자 정보
     */
    @MessageMapping("/chatroom.read")
    public void markAsRead(@Payload Long chatRoomId, Principal principal) {
        try {
            log.debug("읽음 처리 요청 - 채팅방: {}, 사용자: {}", chatRoomId, principal.getName());

            Long profileId = getProfileIdFromPrincipal(principal);
            chatRoomService.markChatRoomMessagesAsRead(profileId, chatRoomId);

            // 읽음 알림을 채팅방에 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatRoomId + "/read",
                    profileId
            );

            log.info("읽음 처리 완료 - 채팅방: {}", chatRoomId);
        } catch (Exception e) {
            log.error("읽음 처리 실패", e);
            sendErrorToUser(principal, "읽음 처리 실패: " + e.getMessage());
        }
    }

    // ===== Admin Endpoints =====

    /**
     * 어드민 전용 - 모든 매칭된 채팅방 리스트 조회
     * @return 모든 매칭된 채팅방 리스트
     */
    @GetMapping("/admin/matched")
    public ResponseEntity<List<ChatRoomListResponse.ChatRoomDto>> getAllMatchedChatRooms() {
        log.debug("어드민 API: 모든 매칭된 채팅방 조회");

        List<ChatRoomListResponse.ChatRoomDto> chatRooms = chatRoomService.getAllMatchedChatRooms();

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 어드민 전용 - 특정 채팅방의 모든 메시지 조회
     * @param chatRoomId 채팅방 ID
     * @return 메시지 리스트
     */
    @GetMapping("/admin/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getAdminChatRoomMessages(
            @PathVariable Long chatRoomId) {

        log.debug("어드민 API: 채팅방 메시지 조회 - 채팅방: {}", chatRoomId);

        List<ChatMessageResponse> messages = chatRoomService.getAdminChatRoomMessages(chatRoomId);

        return ResponseEntity.ok(messages);
    }

    // ===== Private Helper Methods =====

    /**
     * Principal에서 프로필 ID 추출 (WebSocket용)
     */
    private Long getProfileIdFromPrincipal(Principal principal) {
        String email = principal.getName();
        log.debug("WebSocket 프로필 조회 시도 - 이메일: {}", email);

        User user = userRepository.findBySchoolEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        log.debug("사용자 조회 완료 - User ID: {}", user.getId());

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다. 프로필 생성이 필요합니다. (User ID: " + user.getId() + ")"));

        log.debug("프로필 조회 완료 - Profile ID: {}", profile.getId());

        return profile.getId();
    }

    /**
     * OAuth2User에서 프로필 ID 추출 (REST API용)
     */
    private Long getProfileIdFromOAuth2User(CustomOAuth2User oauth2User) {
        String email;

        // OAuth2 인증인 경우
        if (oauth2User != null) {
            email = oauth2User.getEmail();
        } else {
            // JWT 인증인 경우 SecurityContext에서 이메일 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof String) {
                email = (String) authentication.getPrincipal();
            } else {
                throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
            }
        }

        log.debug("프로필 조회 시도 - 이메일: {}", email);

        User user = userRepository.findBySchoolEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        log.debug("사용자 조회 완료 - User ID: {}, 이메일: {}", user.getId(), email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다. 프로필 생성이 필요합니다. (User ID: " + user.getId() + ", 이메일: " + email + ")"));

        log.debug("프로필 조회 완료 - Profile ID: {}", profile.getId());

        return profile.getId();
    }

    /**
     * 사용자에게 에러 메시지 전송
     */
    private void sendErrorToUser(Principal principal, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                errorMessage
        );
    }
}

