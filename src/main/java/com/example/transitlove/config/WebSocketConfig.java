package com.example.transitlove.config;

import com.example.transitlove.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 - STOMP over SockJS
 * 클린 아키텍처의 인프라스트럭처 계층
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 메시지 브로커 설정
     * - /topic: 일대다 브로드캐스트용
     * - /queue: 일대일 메시지용
     * - /app: 애플리케이션 목적지 접두사
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 목적지
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지를 보낼 목적지
        config.setApplicationDestinationPrefixes("/app");

        log.info("메시지 브로커 설정 완료 - 구독: /topic, /queue, 발행: /app");
    }

    /**
     * STOMP 엔드포인트 등록
     * WebSocket 연결을 위한 엔드포인트
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        log.info("STOMP 엔드포인트 등록 완료 - /ws");
    }

    /**
     * 클라이언트 인바운드 채널 설정
     * JWT 토큰 검증 인터셉터 추가
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtAuthenticationInterceptor());
    }

    /**
     * JWT 인증 인터셉터
     * WebSocket 연결 시 JWT 토큰을 검증
     */
    private class JwtAuthenticationInterceptor implements ChannelInterceptor {

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            // CONNECT 명령어인 경우 JWT 토큰 검증
            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                authenticateUser(accessor);
            }

            return message;
        }

        /**
         * 사용자 인증 처리
         */
        private void authenticateUser(StompHeaderAccessor accessor) {
            try {
                String authToken = accessor.getFirstNativeHeader("Authorization");

                if (authToken != null && authToken.startsWith("Bearer ")) {
                    String token = authToken.substring(7);

                    if (jwtTokenProvider.validateToken(token)) {
                        Authentication authentication = jwtTokenProvider.getAuthentication(token);
                        accessor.setUser(authentication);
                        log.debug("WebSocket 연결 인증 성공 - 사용자: {}", authentication.getName());
                    } else {
                        log.warn("유효하지 않은 JWT 토큰으로 WebSocket 연결 시도");
                    }
                } else {
                    log.warn("Authorization 헤더가 없거나 형식이 잘못됨");
                }
            } catch (Exception e) {
                log.error("WebSocket 인증 중 오류 발생", e);
            }
        }
    }
}

