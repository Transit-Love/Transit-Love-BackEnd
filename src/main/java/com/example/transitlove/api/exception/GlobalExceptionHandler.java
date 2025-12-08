package com.example.transitlove.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 정적 리소스 404 예외 처리 (sw.js 등) - 로그만 남기고 조용히 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        // Service Worker 파일 등은 DEBUG 레벨로만 로깅
        if (ex.getMessage().contains("sw.js") || ex.getMessage().contains("manifest.json")) {
            log.debug("프론트엔드 리소스 요청 무시: {}", ex.getResourcePath());
        } else {
            log.warn("리소스를 찾을 수 없음: {}", ex.getResourcePath());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", "요청한 리소스를 찾을 수 없습니다.");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * 채팅방 만료 예외 처리 - 410 Gone
     */
    @ExceptionHandler(ChatRoomExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleChatRoomExpiredException(ChatRoomExpiredException ex) {
        log.warn("채팅방 만료: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.GONE.value());
        body.put("error", "Gone");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.GONE);
    }

    /**
     * IllegalArgumentException 처리 - 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 요청: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 일반 예외 처리 - 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        // 정적 리소스 관련 에러는 이미 처리됨
        if (ex instanceof NoResourceFoundException) {
            return handleNoResourceFoundException((NoResourceFoundException) ex);
        }

        log.error("서버 오류", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "서버 오류가 발생했습니다.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

