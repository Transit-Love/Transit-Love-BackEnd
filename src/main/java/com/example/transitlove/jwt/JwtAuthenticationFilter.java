package com.example.transitlove.jwt;

import com.example.transitlove.oauth2.user.CustomOAuth2User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // OAuth2 인증 경로는 JWT 필터 적용하지 않음
        boolean shouldNotFilter = path.startsWith("/oauth2/") ||
                                  path.startsWith("/login/oauth2/") ||
                                  path.startsWith("/login/oauth2/code/");

        if (shouldNotFilter) {
            log.debug("OAuth2 경로 감지, JWT 필터 제외: {}", path);
        }

        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getUserRole(token);

                // CustomOAuth2User 객체 생성
<<<<<<< HEAD
                CustomOAuth2User customOAuth2User = CustomOAuth2User.create(userId, email, role);

                // Spring Security 인증 객체 생성 (principal을 CustomOAuth2User로 설정)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        customOAuth2User,
                        null,
=======
                CustomOAuth2User customOAuth2User = CustomOAuth2User.create(
                        userId,
                        email,
                        role,
>>>>>>> 39adb0f260320a7597f5784b7af2c6f43ab6a81b
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );

                // Spring Security 인증 객체 생성 - principal을 CustomOAuth2User로 설정
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        customOAuth2User,
                        null,
                        customOAuth2User.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 인증 성공: userId={}, email={}, role={}", userId, email, role);
            } catch (Exception e) {
                log.warn("JWT 인증 실패: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

