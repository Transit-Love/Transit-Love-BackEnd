package com.example.transitlove.service;

import com.example.transitlove.api.dto.ChatListResponse;
import com.example.transitlove.api.dto.ChatMessageRequest;
import com.example.transitlove.api.dto.ChatMessageResponse;
import com.example.transitlove.api.dto.MatchResponse;
import com.example.transitlove.domain.entity.ChatMessage;
import com.example.transitlove.domain.entity.Match;
import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.repository.ChatMessageRepository;
import com.example.transitlove.domain.repository.MatchRepository;
import com.example.transitlove.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 비즈니스 로직을 처리하는 서비스
 * 클린 아키텍처 원칙에 따라 도메인 로직을 캡슐화
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;

    /**
     * 채팅 메시지 전송
     * @param senderProfileId 발신자 프로필 ID
     * @param request 메시지 요청 정보
     * @return 저장된 메시지 응답
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long senderProfileId, ChatMessageRequest request) {
        log.debug("메시지 전송 시도 - 발신자: {}, 매칭: {}", senderProfileId, request.getMatchId());

        // 1. 발신자 프로필 조회
        Profile senderProfile = findProfileById(senderProfileId);

        // 2. 매칭 권한 검증
        Match match = findActiveMatchWithPermission(request.getMatchId(), senderProfileId);

        // 3. 메시지 엔티티 생성
        ChatMessage chatMessage = createChatMessage(match, senderProfile, request);

        // 4. 메시지 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("메시지 저장 완료 - ID: {}", savedMessage.getId());

        return ChatMessageResponse.from(savedMessage);
    }

    /**
     * 채팅 메시지 목록 조회
     * @param profileId 요청자 프로필 ID
     * @param matchId 매칭 ID
     * @return 메시지 목록
     */
    public List<ChatMessageResponse> getMessages(Long profileId, Long matchId) {
        log.debug("메시지 목록 조회 - 프로필: {}, 매칭: {}", profileId, matchId);

        // 매칭 권한 검증
        findActiveMatchWithPermission(matchId, profileId);

        List<ChatMessage> messages = chatMessageRepository.findByMatchIdOrderBySentAtAsc(matchId);

        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 메시지 읽음 처리
     * @param profileId 읽은 사용자 프로필 ID
     * @param matchId 매칭 ID
     */
    @Transactional
    public void markMessagesAsRead(Long profileId, Long matchId) {
        log.debug("메시지 읽음 처리 - 프로필: {}, 매칭: {}", profileId, matchId);

        // 매칭 권한 검증
        findActiveMatchWithPermission(matchId, profileId);

        chatMessageRepository.markAllAsReadByMatchIdAndNotSender(matchId, profileId);
        log.info("메시지 읽음 처리 완료 - 매칭: {}", matchId);
    }

    /**
     * 내 매칭 목록 조회
     * @param profileId 프로필 ID
     * @return 매칭 목록 (미읽은 메시지 수 포함)
     */
    public List<MatchResponse> getMyMatches(Long profileId) {
        log.debug("매칭 목록 조회 - 프로필: {}", profileId);

        List<Match> matches = matchRepository.findActiveMatchesByProfileId(profileId);

        return matches.stream()
                .map(match -> {
                    Long unreadCount = chatMessageRepository.countUnreadMessagesByMatchIdAndNotSender(
                            match.getId(), profileId);
                    return MatchResponse.from(match, profileId, unreadCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 채팅 목록 조회 (매칭된 커플 1개 + 일반 프로필 목록)
     * @param profileId 프로필 ID
     * @return 채팅 목록 (커플 프로필 + 일반 프로필들)
     */
    public ChatListResponse getChatList(Long profileId) {
        log.debug("채팅 목록 조회 - 프로필: {}", profileId);

        // 1. 매칭된 커플 프로필 조회
        List<Match> activeMatches = matchRepository.findActiveMatchesByProfileId(profileId);
        ChatListResponse.MatchedProfileDto matchedProfile = null;

        if (!activeMatches.isEmpty()) {
            // 첫 번째 매칭만 사용 (요구사항: 커플 1개)
            Match match = activeMatches.get(0);
            Profile otherProfile = match.getOtherProfile(profileId);

            // 최신 메시지 조회
            ChatMessage latestMessage = chatMessageRepository.findLatestMessageByMatchId(match.getId());

            // 미읽은 메시지 수 조회
            Long unreadCount = chatMessageRepository.countUnreadMessagesByMatchIdAndNotSender(
                    match.getId(), profileId);

            matchedProfile = ChatListResponse.MatchedProfileDto.builder()
                    .matchId(match.getId())
                    .profileId(otherProfile.getId())
                    .nickname(otherProfile.getNickname())
                    .mbti(otherProfile.getMbti())
                    .matchedAt(match.getMatchedAt())
                    .unreadMessageCount(unreadCount)
                    .lastMessage(latestMessage != null ? latestMessage.getContent() : null)
                    .lastMessageTime(latestMessage != null ? latestMessage.getSentAt() : null)
                    .build();
        }

        // 2. 일반 프로필 목록 조회 (매칭되지 않은 프로필들)
        List<Profile> allProfiles = profileRepository.findAllExcept(profileId);

        // 매칭된 프로필 ID 추출
        List<Long> matchedProfileIds = activeMatches.stream()
                .map(match -> match.getOtherProfile(profileId).getId())
                .collect(Collectors.toList());

        // 매칭되지 않은 프로필만 필터링
        List<ChatListResponse.ProfileListItemDto> otherProfiles = allProfiles.stream()
                .filter(profile -> !matchedProfileIds.contains(profile.getId()))
                .map(profile -> ChatListResponse.ProfileListItemDto.builder()
                        .profileId(profile.getId())
                        .nickname(profile.getNickname())
                        .mbti(profile.getMbti())
                        .createdAt(profile.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        log.info("채팅 목록 조회 완료 - 매칭 프로필: {}, 일반 프로필: {}",
                matchedProfile != null ? 1 : 0, otherProfiles.size());

        return ChatListResponse.builder()
                .matchedProfile(matchedProfile)
                .otherProfiles(otherProfiles)
                .build();
    }

    /**
     * 매칭 생성 (테스트용)
     * @param profile1Id 프로필 1 ID
     * @param profile2Id 프로필 2 ID
     * @return 생성된 매칭
     */
    @Transactional
    public Match createMatch(Long profile1Id, Long profile2Id) {
        log.debug("매칭 생성 시도 - 프로필1: {}, 프로필2: {}", profile1Id, profile2Id);

        // 프로필 조회
        Profile profile1 = findProfileById(profile1Id);
        Profile profile2 = findProfileById(profile2Id);

        // 중복 매칭 검증
        validateDuplicateMatch(profile1Id, profile2Id);

        // 매칭 생성
        Match match = Match.builder()
                .profile1(profile1)
                .profile2(profile2)
                .matchType(Match.MatchType.MANUAL)
                .similarityScore(0.0)
                .isActive(true)
                .build();

        Match savedMatch = matchRepository.save(match);
        log.info("매칭 생성 완료 - ID: {}", savedMatch.getId());

        return savedMatch;
    }

    /**
     * 매칭 해제
     * @param profileId 요청자 프로필 ID
     * @param matchId 매칭 ID
     */
    @Transactional
    public void unmatch(Long profileId, Long matchId) {
        log.debug("매칭 해제 시도 - 프로필: {}, 매칭: {}", profileId, matchId);

        Match match = findActiveMatchWithPermission(matchId, profileId);
        match.unmatch();

        log.info("매칭 해제 완료 - ID: {}", matchId);
    }

    // ===== Private Helper Methods =====

    /**
     * 프로필 조회 (존재하지 않으면 예외)
     */
    private Profile findProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> {
                    log.error("프로필을 찾을 수 없음 - ID: {}", profileId);
                    return new IllegalArgumentException("프로필을 찾을 수 없습니다. ID: " + profileId);
                });
    }

    /**
     * 활성 매칭 조회 및 권한 검증
     */
    private Match findActiveMatchWithPermission(Long matchId, Long profileId) {
        return matchRepository.findActiveMatchByIdAndProfileId(matchId, profileId)
                .orElseThrow(() -> {
                    log.error("활성화된 매칭을 찾을 수 없거나 권한이 없음 - 매칭: {}, 프로필: {}", matchId, profileId);
                    return new IllegalArgumentException("활성화된 매칭을 찾을 수 없거나 접근 권한이 없습니다.");
                });
    }

    /**
     * 중복 매칭 검증
     */
    private void validateDuplicateMatch(Long profile1Id, Long profile2Id) {
        matchRepository.findActiveMatchBetweenProfiles(profile1Id, profile2Id)
                .ifPresent(m -> {
                    log.error("이미 매칭이 존재함 - 프로필1: {}, 프로필2: {}", profile1Id, profile2Id);
                    throw new IllegalStateException("이미 활성 매칭이 존재합니다.");
                });
    }

    /**
     * 채팅 메시지 엔티티 생성
     */
    private ChatMessage createChatMessage(Match match, Profile senderProfile, ChatMessageRequest request) {
        return ChatMessage.builder()
                .match(match)
                .senderProfile(senderProfile)
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : ChatMessage.MessageType.TEXT)
                .isRead(false)
                .build();
    }
}

