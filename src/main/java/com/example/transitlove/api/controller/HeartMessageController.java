package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.HeartMessageRequest;
import com.example.transitlove.api.dto.HeartMessageResponse;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.service.HeartMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/heart-messages")
@RequiredArgsConstructor
public class HeartMessageController {

    private final HeartMessageService heartMessageService;

    /**
     * 속마음문자 전송
     * POST /api/heart-messages
     */
    @PostMapping
    public ResponseEntity<HeartMessageResponse> sendHeartMessage(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Valid @RequestBody HeartMessageRequest request
    ) {
        HeartMessageResponse response = heartMessageService.sendHeartMessage(oAuth2User.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 속마음문자 목록 조회 (보낸 것과 받은 것 모두)
     * GET /api/heart-messages
     */
    @GetMapping
    public ResponseEntity<List<HeartMessageResponse>> getHeartMessages(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        List<HeartMessageResponse> responses = heartMessageService.getHeartMessages(oAuth2User.getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 받은 속마음문자 목록 조회
     * GET /api/heart-messages/received
     */
    @GetMapping("/received")
    public ResponseEntity<List<HeartMessageResponse>> getReceivedHeartMessages(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        List<HeartMessageResponse> responses = heartMessageService.getReceivedHeartMessages(oAuth2User.getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 보낸 속마음문자 목록 조회
     * GET /api/heart-messages/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<List<HeartMessageResponse>> getSentHeartMessages(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        List<HeartMessageResponse> responses = heartMessageService.getSentHeartMessages(oAuth2User.getId());
        return ResponseEntity.ok(responses);
    }
}