package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.AdminChatMessagesResponse;
import com.example.transitlove.api.dto.AdminMatchListResponse;
import com.example.transitlove.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 어드민 전용 채팅 API 컨트롤러
 * - 모든 커플 매칭 목록 조회
 * - 특정 커플의 채팅 내역 조회
 *
 * 주의: 어드민만 접근 가능하며, 조회만 가능 (메시지 전송 불가)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminChatController {

    private final ChatService chatService;

    /**
     * 모든 커플 매칭 목록 조회
     *
     * @param includeInactive 비활성 매칭 포함 여부 (기본값: false)
     * @return 모든 매칭 목록 (활성/비활성 여부, 메시지 수, 최근 메시지 포함)
     *
     * 예시:
     * GET /api/admin/chat/matches
     * GET /api/admin/chat/matches?includeInactive=true
     */
    @GetMapping("/matches")
    public ResponseEntity<AdminMatchListResponse> getAllMatches(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {

        log.info("어드민 - 모든 매칭 조회 요청 (비활성 포함: {})", includeInactive);

        AdminMatchListResponse response = chatService.getAllMatches(includeInactive);

        log.info("어드민 - 매칭 조회 완료: {} 건", response.getTotalCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 커플의 모든 채팅 메시지 조회
     *
     * @param matchId 매칭 ID
     * @return 채팅 메시지 목록 (매칭 정보 포함)
     *
     * 예시:
     * GET /api/admin/chat/matches/1/messages
     */
    @GetMapping("/matches/{matchId}/messages")
    public ResponseEntity<AdminChatMessagesResponse> getMatchMessages(
            @PathVariable Long matchId) {

        log.info("어드민 - 매칭 메시지 조회 요청: matchId={}", matchId);

        AdminChatMessagesResponse response = chatService.getMatchMessages(matchId);

        log.info("어드민 - 매칭 메시지 조회 완료: {} 건", response.getTotalMessageCount());

        return ResponseEntity.ok(response);
    }
}

