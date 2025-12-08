package com.example.transitlove.oauth2.handler;

import com.example.transitlove.jwt.JwtTokenProvider;
import com.example.transitlove.oauth2.user.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        log.info("=== OAuth2 인증 성공 ===");
        log.info("Authentication 타입: {}", authentication.getClass().getName());
        log.info("Principal 타입: {}", authentication.getPrincipal().getClass().getName());

        String targetUrl = determineTargetUrl(request, response, authentication);
        log.info("리다이렉트 URL: {}", targetUrl);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        log.info("사용자 정보 - ID: {}, Email: {}, Role: {}",
                oAuth2User.getId(), oAuth2User.getEmail(), oAuth2User.getRole());

        // JWT 토큰 생성 (userId 포함)
        String token = jwtTokenProvider.createToken(oAuth2User.getId(), oAuth2User.getEmail(), oAuth2User.getRole());
        log.info("JWT 토큰 생성 완료 - userId: {}, 길이: {}", oAuth2User.getId(), token.length());
        log.debug("생성된 토큰: {}", token.substring(0, Math.min(50, token.length())) + "...");

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("role", oAuth2User.getRole())
                .build().toUriString();

        return targetUrl;
    }
}