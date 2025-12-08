package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.*;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 채팅 API 컨트롤러
 * - WebSocket 메시지 핸들러
 * - REST API 엔드포인트
 * 클린 아키텍처의 프레젠테이션 계층
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    // ===== WebSocket Message Handlers =====

    /**
     * 메시지 전송 핸들러
     * @param request 메시지 요청
     * @param principal 인증된 사용자 정보
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        try {
            log.debug("WebSocket 메시지 수신 - 매칭: {}, 사용자: {}", request.getMatchId(), principal.getName());

            Long profileId = getProfileIdFromPrincipal(principal);
            ChatMessageResponse response = chatService.sendMessage(profileId, request);

            // 매칭된 채팅방의 모든 참여자에게 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + request.getMatchId(),
                    response
            );

            log.info("메시지 브로드캐스트 완료 - 메시지 ID: {}", response.getId());
        } catch (Exception e) {
            log.error("메시지 전송 실패", e);
            sendErrorToUser(principal, "메시지 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 메시지 읽음 처리 핸들러
     * @param matchId 매칭 ID
     * @param principal 인증된 사용자 정보
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Long matchId, Principal principal) {
        try {
            log.debug("읽음 처리 요청 - 매칭: {}, 사용자: {}", matchId, principal.getName());

            Long profileId = getProfileIdFromPrincipal(principal);
            chatService.markMessagesAsRead(profileId, matchId);

            // 읽음 알림을 채팅방에 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + matchId + "/read",
                    profileId
            );

            log.info("읽음 처리 완료 - 매칭: {}", matchId);
        } catch (Exception e) {
            log.error("읽음 처리 실패", e);
            sendErrorToUser(principal, "읽음 처리 실패: " + e.getMessage());
        }
    }

    // ===== REST API Endpoints =====

    /**
     * 채팅 메시지 목록 조회
     * @param matchId 매칭 ID
     * @param oauth2User 인증된 사용자
     * @return 메시지 목록
     */
    @GetMapping("/api/chat/matches/{matchId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long matchId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 메시지 목록 조회 - 매칭: {}", matchId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        List<ChatMessageResponse> messages = chatService.getMessages(profileId, matchId);

        return ResponseEntity.ok(messages);
    }

    /**
     * 내 매칭 목록 조회
     * @param oauth2User 인증된 사용자
     * @return 매칭 목록
     */
    @GetMapping("/api/chat/matches")
    public ResponseEntity<List<MatchResponse>> getMyMatches(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 매칭 목록 조회");

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        List<MatchResponse> matches = chatService.getMyMatches(profileId);

        return ResponseEntity.ok(matches);
    }

    /**
     * 채팅 목록 조회 (커플 프로필 1개 + 일반 프로필 목록)
     * @param oauth2User 인증된 사용자
     * @return 채팅 목록
     */
    @GetMapping("/api/chat/list")
    public ResponseEntity<ChatListResponse> getChatList(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 채팅 목록 조회 (커플 + 일반 프로필)");

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        ChatListResponse chatList = chatService.getChatList(profileId);

        return ResponseEntity.ok(chatList);
    }

    /**
     * 매칭 생성 (테스트용)
     * @param targetProfileId 대상 프로필 ID
     * @param oauth2User 인증된 사용자
     * @return 성공 메시지
     */
    @PostMapping("/api/chat/matches")
    public ResponseEntity<String> createMatch(
            @RequestParam Long targetProfileId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 매칭 생성 - 대상 프로필: {}", targetProfileId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        chatService.createMatch(profileId, targetProfileId);

        return ResponseEntity.ok("매칭이 생성되었습니다.");
    }

    /**
     * 매칭 해제
     * @param matchId 매칭 ID
     * @param oauth2User 인증된 사용자
     * @return 성공 메시지
     */
    @DeleteMapping("/api/chat/matches/{matchId}")
    public ResponseEntity<String> unmatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 매칭 해제 - 매칭: {}", matchId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        chatService.unmatch(profileId, matchId);

        return ResponseEntity.ok("매칭이 해제되었습니다.");
    }

    /**
     * 메시지 읽음 처리
     * @param matchId 매칭 ID
     * @param oauth2User 인증된 사용자
     * @return 성공 메시지
     */
    @PutMapping("/api/chat/matches/{matchId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Long matchId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 읽음 처리 - 매칭: {}", matchId);

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        chatService.markMessagesAsRead(profileId, matchId);

        return ResponseEntity.ok("메시지가 읽음 처리되었습니다.");
    }

    // ===== Final Choice API Endpoints =====

    /**
     * 마지막 선택 옵션 조회
     * GET /api/chat/final-choice/options
     *
     * @param oauth2User 인증된 사용자
     * @return 현재 매칭 상대 + 채팅했던 다른 사람들
     */
    @GetMapping("/api/chat/final-choice/options")
    public ResponseEntity<FinalChoiceOptionsResponse> getFinalChoiceOptions(
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 마지막 선택 옵션 조회");

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        FinalChoiceOptionsResponse options = chatService.getFinalChoiceOptions(profileId);

        return ResponseEntity.ok(options);
    }

    /**
     * 마지막 선택 실행
     * POST /api/chat/final-choice
     *
     * @param request 선택한 프로필 ID
     * @param oauth2User 인증된 사용자
     * @return 최종 매칭 정보
     */
    @PostMapping("/api/chat/final-choice")
    public ResponseEntity<FinalChoiceResponse> makeFinalChoice(
            @RequestBody FinalChoiceRequest request,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.debug("REST API: 마지막 선택 실행 - 선택 프로필: {}", request.getSelectedProfileId());

        Long profileId = getProfileIdFromOAuth2User(oauth2User);
        FinalChoiceResponse response = chatService.makeFinalChoice(profileId, request.getSelectedProfileId());

        return ResponseEntity.ok(response);
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
        // JWT 필터에서 CustomOAuth2User를 생성하므로 null이 아님
        if (oauth2User == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }

        String email = oauth2User.getEmail();
        log.debug("프로필 조회 시도 - 이메일: {}", email);

        User user = userRepository.findBySchoolEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        log.debug("사용자 조회 완료 - User ID: {}, 이메일: {}", user.getId(), email);

        // findByUser 대신 findByUserId 사용
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

