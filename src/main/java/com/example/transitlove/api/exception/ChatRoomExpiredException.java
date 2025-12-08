package com.example.transitlove.api.exception;

/**
 * 채팅방 만료 예외
 * HTTP 410 Gone 상태 코드로 반환됨
 */
public class ChatRoomExpiredException extends RuntimeException {
    public ChatRoomExpiredException(String message) {
        super(message);
    }

    public ChatRoomExpiredException(Long chatRoomId) {
        super("채팅방이 만료되었습니다. 채팅방 ID: " + chatRoomId);
    }
}

