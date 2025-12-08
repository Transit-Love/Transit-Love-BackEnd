package com.example.transitlove.api.controller;

import com.example.transitlove.domain.entity.User;
import com.example.transitlove.domain.repository.UserRepository;
import com.example.transitlove.jwt.JwtTokenProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 테스트용 인증 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 테스트용 로그인 (이메일만으로 토큰 발급)
     * POST /api/auth/test-login
     */
    @PostMapping("/test-login")
    public ResponseEntity<Map<String, String>> testLogin(@RequestBody LoginRequest request) {
        log.info("테스트 로그인 요청: {}", request.getEmail());

        User user = userRepository.findBySchoolEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getEmail()));

        // JWT 토큰 생성 (userId 포함)
        String token = jwtTokenProvider.createToken(user.getId(), user.getSchoolEmail(), user.getRole().name());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("email", user.getSchoolEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole().name());

        log.info("토큰 발급 완료: userId={}, name={}, role={}", user.getId(), user.getName(), user.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findBySchoolEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Map<String, String> response = new HashMap<>();
        response.put("email", user.getSchoolEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole().name());

        return ResponseEntity.ok(response);
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
    }
}

