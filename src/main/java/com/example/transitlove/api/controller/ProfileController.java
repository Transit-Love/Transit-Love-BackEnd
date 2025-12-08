package com.example.transitlove.api.controller;

import com.example.transitlove.api.dto.ProfileCreateRequest;
import com.example.transitlove.api.dto.ProfileResponse;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import com.example.transitlove.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        ProfileResponse response = profileService.createProfile(oAuth2User.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        ProfileResponse response = profileService.getProfile(oAuth2User.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        ProfileResponse response = profileService.updateProfile(oAuth2User.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭된 프로필 목록 조회 (나와 매칭된 상대방들)
     * GET /api/profiles/matched
     */
    @GetMapping("/matched")
    public ResponseEntity<List<ProfileResponse>> getMatchedProfiles(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        List<ProfileResponse> responses = profileService.getMatchedProfiles(oAuth2User.getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 매칭되지 않은 프로필 목록 조회 (매칭 가능한 후보들)
     * GET /api/profiles/unmatched
     */
    @GetMapping("/unmatched")
    public ResponseEntity<List<ProfileResponse>> getUnmatchedProfiles(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        List<ProfileResponse> responses = profileService.getUnmatchedProfiles(oAuth2User.getId());
        return ResponseEntity.ok(responses);
    }
}

