package com.example.transitlove.api.controller;

import com.example.transitlove.domain.entity.VoiceCallHistory;
import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.service.VoiceCallManagementService;
import com.example.transitlove.domain.vo.*;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 음성 통화 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceCallManagementController {

    private final VoiceCallManagementService voiceCallManagementService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * 음소거 상태 변경
     * POST /api/voice/room/{chatRoomId}/mute
     */
    @PostMapping("/room/{chatRoomId}/mute")
    public ResponseEntity<Map<String, Object>> updateMuteStatus(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody VoiceMuteRequest request,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("음소거 상태 변경 요청 - 채팅방: {}, 요청: {}", chatRoomId, request);

        try {
            // 권한 검증 (필요시)
            // User user = userRepository.findBySchoolEmail(oauth2User.getEmail())
            //         .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            VoiceMuteResponse response = voiceCallManagementService.updateMuteStatus(chatRoomId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "음소거 상태가 업데이트되었습니다.");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("음소거 상태 변경 실패: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("errorCode", "INVALID_REQUEST");
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("음소거 상태 변경 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 음소거 상태 조회
     * GET /api/voice/room/{chatRoomId}/mute/{profileId}
     */
    @GetMapping("/room/{chatRoomId}/mute/{profileId}")
    public ResponseEntity<Map<String, Object>> getMuteStatus(
            @PathVariable Long chatRoomId,
            @PathVariable Long profileId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("음소거 상태 조회 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);

        try {
            VoiceMuteResponse response = voiceCallManagementService.getMuteStatus(chatRoomId, profileId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "음소거 상태를 조회했습니다.");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("음소거 상태 조회 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 통화 기록 저장
     * POST /api/voice/call/history
     */
    @PostMapping("/call/history")
    public ResponseEntity<Map<String, Object>> saveCallHistory(
            @Valid @RequestBody VoiceCallHistoryRequest request,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("통화 기록 저장 요청 - 채팅방: {}, 발신자: {}, 수신자: {}",
                request.getChatRoomId(), request.getCallerProfileId(), request.getReceiverProfileId());

        try {
            // 권한 검증 (필요시)
            // User user = userRepository.findBySchoolEmail(oauth2User.getEmail())
            //         .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            VoiceCallHistory history = voiceCallManagementService.saveCallHistory(request);

            Map<String, Object> data = new HashMap<>();
            data.put("callHistoryId", history.getId());
            data.put("chatRoomId", history.getChatRoomId());
            data.put("callerProfileId", history.getCallerProfileId());
            data.put("receiverProfileId", history.getReceiverProfileId());
            data.put("startTime", history.getStartTime());
            data.put("endTime", history.getEndTime());
            data.put("duration", history.getDuration());
            data.put("callStatus", history.getCallStatus().name());
            data.put("voiceModulationUsed", history.getVoiceModulationUsed());
            data.put("createdAt", history.getCreatedAt());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "통화 기록이 저장되었습니다.");
            result.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            log.error("통화 기록 저장 실패: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("errorCode", "INVALID_REQUEST");
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("통화 기록 저장 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 통화 기록 조회 (특정 채팅방)
     * GET /api/voice/call/history?chatRoomId={chatRoomId}&page={page}&size={size}
     */
    @GetMapping("/call/history")
    public ResponseEntity<Map<String, Object>> getCallHistory(
            @RequestParam Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("통화 기록 조회 - 채팅방: {}, 페이지: {}, 크기: {}", chatRoomId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<VoiceCallHistory> historyPage = voiceCallManagementService.getCallHistoryByChatRoom(chatRoomId, pageable);

            Map<String, Object> data = new HashMap<>();
            data.put("content", historyPage.getContent());
            data.put("pageable", Map.of(
                    "pageNumber", historyPage.getNumber(),
                    "pageSize", historyPage.getSize()
            ));
            data.put("totalElements", historyPage.getTotalElements());
            data.put("totalPages", historyPage.getTotalPages());
            data.put("last", historyPage.isLast());
            data.put("first", historyPage.isFirst());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "통화 기록을 조회했습니다.");
            result.put("data", data);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("통화 기록 조회 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 통화 기록 조회 (내 프로필)
     * GET /api/voice/call/history/my?page={page}&size={size}
     */
    @GetMapping("/call/history/my")
    public ResponseEntity<Map<String, Object>> getMyCallHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("내 통화 기록 조회 - 페이지: {}, 크기: {}", page, size);

        try {
            User user = userRepository.findBySchoolEmail(oauth2User.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

            Long profileId = profile.getId();
            Pageable pageable = PageRequest.of(page, size);
            Page<VoiceCallHistory> historyPage = voiceCallManagementService.getMyCallHistory(profileId, pageable);

            Map<String, Object> data = new HashMap<>();
            data.put("content", historyPage.getContent());
            data.put("pageable", Map.of(
                    "pageNumber", historyPage.getNumber(),
                    "pageSize", historyPage.getSize()
            ));
            data.put("totalElements", historyPage.getTotalElements());
            data.put("totalPages", historyPage.getTotalPages());
            data.put("last", historyPage.isLast());
            data.put("first", historyPage.isFirst());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "통화 기록을 조회했습니다.");
            result.put("data", data);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("내 통화 기록 조회 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 통화 통계 조회
     * GET /api/voice/call/statistics?chatRoomId={chatRoomId}
     */
    @GetMapping("/call/statistics")
    public ResponseEntity<Map<String, Object>> getCallStatistics(
            @RequestParam Long chatRoomId,
            @AuthenticationPrincipal CustomOAuth2User oauth2User) {

        log.info("통화 통계 조회 - 채팅방: {}", chatRoomId);

        try {
            VoiceCallStatisticsResponse statistics = voiceCallManagementService.getCallStatistics(chatRoomId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "통화 통계를 조회했습니다.");
            result.put("data", statistics);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("통화 통계 조회 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            error.put("errorCode", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

