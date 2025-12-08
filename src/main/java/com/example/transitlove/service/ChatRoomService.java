package com.example.transitlove.service;

import com.example.transitlove.api.dto.*;
import com.example.transitlove.api.exception.ChatRoomExpiredException;
import com.example.transitlove.domain.entity.ChatMessage;
import com.example.transitlove.domain.entity.ChatRoom;
import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.ChatMessageRepository;
import com.example.transitlove.domain.repository.ChatRoomRepository;
import com.example.transitlove.domain.repository.MatchRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 기반 채팅 서비스
 * 매칭 여부와 관계없이 모든 사용자 간 채팅 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;

    /**
     * 채팅방 생성 또는 조회
     * 이미 존재하면 기존 채팅방 반환, 없으면 새로 생성
     * @param myProfileId 내 프로필 ID
     * @param targetProfileId 상대방 프로필 ID
     * @return 채팅방 정보
     */
    @Transactional
    public ChatRoomResponse getOrCreateChatRoom(Long myProfileId, Long targetProfileId) {
        log.debug("채팅방 조회/생성 - 내 프로필: {}, 상대 프로필: {}", myProfileId, targetProfileId);

        // 자기 자신과의 채팅방은 생성 불가
        if (myProfileId.equals(targetProfileId)) {
            throw new IllegalArgumentException("자기 자신과는 채팅할 수 없습니다.");
        }

        // 기존 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByProfileIds(myProfileId, targetProfileId)
                .orElseGet(() -> {
                    // 채팅방이 없으면 생성
                    log.info("새 채팅방 생성 - 프로필1: {}, 프로필2: {}", myProfileId, targetProfileId);
                    return createNewChatRoom(myProfileId, targetProfileId);
                });

        log.info("채팅방 조회/생성 완료 - 채팅방 ID: {}", chatRoom.getId());
        return ChatRoomResponse.from(chatRoom, myProfileId);
    }

    /**
     * 내 채팅방 리스트 조회
     * @param profileId 프로필 ID
     * @return 채팅방 리스트 (매칭/일반 구분)
     */
    public ChatRoomListResponse getMyChatRooms(Long profileId) {
        log.debug("채팅방 리스트 조회 - 프로필: {}", profileId);

        // 매칭된 채팅방
        List<ChatRoom> matchedRooms = chatRoomRepository.findMatchedRoomsByProfileId(profileId);
        List<ChatRoomListResponse.ChatRoomDto> matchedDtos = matchedRooms.stream()
                .map(room -> convertToChatRoomDto(room, profileId))
                .collect(Collectors.toList());

        // 매칭되지 않은 채팅방
        List<ChatRoom> unmatchedRooms = chatRoomRepository.findUnmatchedRoomsByProfileId(profileId);
        List<ChatRoomListResponse.ChatRoomDto> unmatchedDtos = unmatchedRooms.stream()
                .map(room -> convertToChatRoomDto(room, profileId))
                .collect(Collectors.toList());

        log.info("채팅방 리스트 조회 완료 - 매칭: {}, 일반: {}", matchedDtos.size(), unmatchedDtos.size());

        return ChatRoomListResponse.builder()
                .matchedRooms(matchedDtos)
                .unmatchedRooms(unmatchedDtos)
                .totalCount(matchedDtos.size() + unmatchedDtos.size())
                .build();
    }

    /**
     * 채팅방 메시지 조회
     * @param profileId 요청자 프로필 ID
     * @param chatRoomId 채팅방 ID
     * @return 메시지 리스트
     */
    public List<ChatMessageResponse> getChatRoomMessages(Long profileId, Long chatRoomId) {
        log.debug("채팅방 메시지 조회 - 프로필: {}, 채팅방: {}", profileId, chatRoomId);

        // 채팅방 접근 권한 확인
        findChatRoomWithPermission(chatRoomId, profileId);

        // 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방에 메시지 전송
     * @param senderProfileId 발신자 프로필 ID
     * @param request 메시지 요청
     * @return 전송된 메시지
     */
    @Transactional
    public ChatMessageResponse sendMessageToChatRoom(Long senderProfileId, ChatRoomMessageRequest request) {
        log.debug("채팅방 메시지 전송 - 발신자: {}, 채팅방: {}", senderProfileId, request.getChatRoomId());

        // 발신자 프로필 조회
        Profile senderProfile = findProfileById(senderProfileId);

        // 채팅방 접근 권한 확인
        ChatRoom chatRoom = findChatRoomWithPermission(request.getChatRoomId(), senderProfileId);

        // 메시지 엔티티 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .match(chatRoom.getMatch())  // 매칭된 채팅방이면 설정
                .senderProfile(senderProfile)
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : ChatMessage.MessageType.TEXT)
                .isRead(false)
                .build();

        // 메시지 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 채팅방 마지막 메시지 시간 업데이트
        chatRoom.updateLastMessageTime();

        log.info("채팅방 메시지 전송 완료 - 메시지 ID: {}", savedMessage.getId());

        return ChatMessageResponse.from(savedMessage);
    }

    /**
     * 채팅방 메시지 읽음 처리
     * @param profileId 읽은 사용자 프로필 ID
     * @param chatRoomId 채팅방 ID
     */
    @Transactional
    public void markChatRoomMessagesAsRead(Long profileId, Long chatRoomId) {
        log.debug("채팅방 메시지 읽음 처리 - 프로필: {}, 채팅방: {}", profileId, chatRoomId);

        // 채팅방 접근 권한 확인
        findChatRoomWithPermission(chatRoomId, profileId);

        // 읽음 처리
        chatMessageRepository.markAllAsReadByChatRoomIdAndNotSender(chatRoomId, profileId);

        log.info("채팅방 메시지 읽음 처리 완료 - 채팅방: {}", chatRoomId);
    }

    /**
     * 채팅방 비활성화
     * @param profileId 요청자 프로필 ID
     * @param chatRoomId 채팅방 ID
     */
    @Transactional
    public void deactivateChatRoom(Long profileId, Long chatRoomId) {
        log.debug("채팅방 비활성화 - 프로필: {}, 채팅방: {}", profileId, chatRoomId);

        ChatRoom chatRoom = findChatRoomWithPermission(chatRoomId, profileId);
        chatRoom.deactivate();

        log.info("채팅방 비활성화 완료 - 채팅방: {}", chatRoomId);
    }

    // ===== Private Helper Methods =====

    /**
     * 새 채팅방 생성
     */
    @Transactional
    protected ChatRoom createNewChatRoom(Long profile1Id, Long profile2Id) {
        Profile profile1 = findProfileById(profile1Id);
        Profile profile2 = findProfileById(profile2Id);

        // 두 프로필 간 매칭 조회
        Match match = matchRepository.findActiveMatchBetweenProfiles(profile1Id, profile2Id)
                .orElse(null);

        ChatRoom chatRoom = ChatRoom.builder()
                .profile1(profile1)
                .profile2(profile2)
                .match(match)
                .roomType(match != null ? ChatRoom.RoomType.MATCHED : ChatRoom.RoomType.UNMATCHED)
                .isActive(true)
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 프로필 조회
     */
    private Profile findProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> {
                    log.error("프로필을 찾을 수 없음 - ID: {}", profileId);
                    return new IllegalArgumentException("프로필을 찾을 수 없습니다. ID: " + profileId);
                });
    }

    /**
     * 채팅방 조회 및 접근 권한 확인
     */
    private ChatRoom findChatRoomWithPermission(Long chatRoomId, Long profileId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("채팅방을 찾을 수 없음 - ID: {}", chatRoomId);
                    return new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId);
                });

        if (!chatRoom.isParticipant(profileId)) {
            log.error("채팅방 접근 권한 없음 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);
            throw new IllegalArgumentException("채팅방에 접근할 권한이 없습니다.");
        }

        // 만료 확인
        if (chatRoom.isExpired()) {
            log.warn("만료된 채팅방 접근 시도 - 채팅방: {}, 만료 시간: {}", chatRoomId, chatRoom.getExpiresAt());
            throw new ChatRoomExpiredException(chatRoomId);
        }

        return chatRoom;
    }

    /**
     * 채팅방 DTO로 변환
     */
    private ChatRoomListResponse.ChatRoomDto convertToChatRoomDto(ChatRoom chatRoom, Long myProfileId) {
        ChatMessage lastMessage = chatMessageRepository.findLatestMessageByChatRoomId(chatRoom.getId());
        Long unreadCount = chatMessageRepository.countUnreadMessagesByChatRoomIdAndNotSender(
                chatRoom.getId(), myProfileId);

        return ChatRoomListResponse.ChatRoomDto.from(
                chatRoom,
                myProfileId,
                lastMessage != null ? lastMessage.getContent() : null,
                unreadCount
        );
    }

    // ===== Admin Methods =====

    /**
     * 어드민 전용 - 모든 매칭된 채팅방 리스트 조회
     * @return 모든 매칭된 채팅방 리스트
     */
    public List<ChatRoomListResponse.ChatRoomDto> getAllMatchedChatRooms() {
        log.debug("어드민 - 모든 매칭된 채팅방 조회");

        List<ChatRoom> matchedRooms = chatRoomRepository.findAllMatchedRooms();

        List<ChatRoomListResponse.ChatRoomDto> result = matchedRooms.stream()
                .map(room -> {
                    ChatMessage lastMessage = chatMessageRepository.findLatestMessageByChatRoomId(room.getId());
                    Long messageCount = chatMessageRepository.countByMatchId(
                            room.getMatch() != null ? room.getMatch().getId() : null);

                    return ChatRoomListResponse.ChatRoomDto.from(
                            room,
                            room.getProfile1().getId(),  // 임시로 profile1 관점
                            lastMessage != null ? lastMessage.getContent() : null,
                            messageCount
                    );
                })
                .collect(Collectors.toList());

        log.info("어드민 - 매칭된 채팅방 조회 완료: {} 건", result.size());
        return result;
    }

    /**
     * 어드민 전용 - 특정 채팅방의 모든 메시지 조회 (권한 확인 없음)
     * @param chatRoomId 채팅방 ID
     * @return 메시지 리스트
     */
    public List<ChatMessageResponse> getAdminChatRoomMessages(Long chatRoomId) {
        log.debug("어드민 - 채팅방 메시지 조회: {}", chatRoomId);

        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("채팅방을 찾을 수 없음 - ID: {}", chatRoomId);
                    return new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId);
                });

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

        log.info("어드민 - 채팅방 메시지 조회 완료: {} 건", messages.size());

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }
}

