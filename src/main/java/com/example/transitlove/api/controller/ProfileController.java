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
        // 임시: 테스트용 - oAuth2User가 null이면 userId 1 사용
        Long userId = (oAuth2User != null) ? oAuth2User.getId() : 1L;
        ProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User
    ) {
        // 임시: 테스트용 - oAuth2User가 null이면 userId 1 사용
        Long userId = (oAuth2User != null) ? oAuth2User.getId() : 1L;
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        // 임시: 테스트용 - oAuth2User가 null이면 userId 1 사용
        Long userId = (oAuth2User != null) ? oAuth2User.getId() : 1L;
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
