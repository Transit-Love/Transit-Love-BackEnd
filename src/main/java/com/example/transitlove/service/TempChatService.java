package com.example.transitlove.service;

import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.TempChat;
import com.example.transitlove.domain.entity.TempChatMessage;
import com.example.transitlove.domain.repository.ProfileRepository;
import com.example.transitlove.domain.repository.TempChatMessageRepository;
import com.example.transitlove.domain.repository.TempChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 임시 채팅 애플리케이션 서비스
 * DDD - Application Service
 * 2시간 제한 채팅 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TempChatService {

    private final TempChatRepository tempChatRepository;
    private final TempChatMessageRepository tempChatMessageRepository;
    private final ProfileRepository profileRepository;

    /**
     * 임시 채팅 생성
     * Use Case: 일반 사용자와 임시 채팅 시작
     */
    @Transactional
    public TempChat createTempChat(Long profile1Id, Long profile2Id) {
        log.info("임시 채팅 생성 시도 - 프로필1: {}, 프로필2: {}", profile1Id, profile2Id);

        // 1. 프로필 조회
        Profile profile1 = findProfileById(profile1Id);
        Profile profile2 = findProfileById(profile2Id);

        // 2. 자기 자신과는 채팅 불가
        if (profile1Id.equals(profile2Id)) {
            throw new IllegalArgumentException("자기 자신과는 채팅할 수 없습니다.");
        }

        // 3. 이미 활성 임시 채팅이 있는지 확인
        tempChatRepository.findActiveTempChatBetweenProfiles(profile1Id, profile2Id)
                .ifPresent(tc -> {
                    if (!tc.isExpired()) {
                        throw new IllegalStateException("이미 활성 임시 채팅이 존재합니다.");
                    }
                });

        // 4. 임시 채팅 생성
        TempChat tempChat = TempChat.builder()
                .profile1(profile1)
                .profile2(profile2)
                .isActive(true)
                .build();

        TempChat savedTempChat = tempChatRepository.save(tempChat);

        log.info("임시 채팅 생성 완료 - ID: {}, 만료 시간: {}",
                savedTempChat.getId(),
                savedTempChat.getExpiresAt());

        return savedTempChat;
    }

    /**
     * 임시 채팅 메시지 전송
     */
    @Transactional
    public TempChatMessage sendMessage(Long senderProfileId, Long tempChatId, String content, TempChatMessage.MessageType type) {
        log.debug("임시 채팅 메시지 전송 - 발신자: {}, 채팅: {}", senderProfileId, tempChatId);

        // 1. 발신자 프로필 조회
        Profile senderProfile = findProfileById(senderProfileId);

        // 2. 임시 채팅 조회 및 권한 확인
        TempChat tempChat = findActiveTempChatWithPermission(tempChatId, senderProfileId);

        // 3. 만료 확인
        if (tempChat.isExpired()) {
            tempChat.expire();
            tempChatRepository.save(tempChat);
            throw new IllegalStateException("임시 채팅이 만료되었습니다. (2시간 제한)");
        }

        // 4. 메시지 생성 및 저장
        TempChatMessage message = TempChatMessage.builder()
                .tempChat(tempChat)
                .senderProfile(senderProfile)
                .content(content)
                .type(type != null ? type : TempChatMessage.MessageType.TEXT)
                .isRead(false)
                .build();

        TempChatMessage savedMessage = tempChatMessageRepository.save(message);

        log.info("임시 채팅 메시지 저장 완료 - ID: {}, 남은 시간: {}분",
                savedMessage.getId(),
                tempChat.getRemainingMinutes());

        return savedMessage;
    }

    /**
     * 임시 채팅 메시지 목록 조회
     */
    public List<TempChatMessage> getMessages(Long profileId, Long tempChatId) {
        log.debug("임시 채팅 메시지 조회 - 프로필: {}, 채팅: {}", profileId, tempChatId);

        TempChat tempChat = findActiveTempChatWithPermission(tempChatId, profileId);

        // 만료 확인 (조회는 가능하지만 경고 로그)
        if (tempChat.isExpired()) {
            log.warn("만료된 임시 채팅 조회 - ID: {}", tempChatId);
        }

        return tempChatMessageRepository.findByTempChatIdOrderBySentAtAsc(tempChatId);
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesAsRead(Long profileId, Long tempChatId) {
        log.debug("임시 채팅 읽음 처리 - 프로필: {}, 채팅: {}", profileId, tempChatId);

        findActiveTempChatWithPermission(tempChatId, profileId);
        tempChatMessageRepository.markAllAsReadByTempChatIdAndNotSender(tempChatId, profileId);

        log.info("임시 채팅 읽음 처리 완료 - 채팅: {}", tempChatId);
    }

    /**
     * 내 임시 채팅 목록 조회
     */
    public List<TempChat> getMyTempChats(Long profileId) {
        log.debug("임시 채팅 목록 조회 - 프로필: {}", profileId);

        List<TempChat> tempChats = tempChatRepository.findActiveTempChatsByProfileId(profileId);

        // 만료된 채팅 필터링 (활성이지만 시간이 지난 것)
        return tempChats.stream()
                .filter(tc -> !tc.isExpired())
                .toList();
    }

    /**
     * 임시 채팅 종료
     */
    @Transactional
    public void endTempChat(Long profileId, Long tempChatId) {
        log.info("임시 채팅 종료 - 프로필: {}, 채팅: {}", profileId, tempChatId);

        TempChat tempChat = findActiveTempChatWithPermission(tempChatId, profileId);
        tempChat.expire();

        log.info("임시 채팅 종료 완료 - ID: {}", tempChatId);
    }

    /**
     * 만료된 임시 채팅 자동 정리 (스케줄러)
     * 매 10분마다 실행
     */
    @Scheduled(fixedRate = 600000) // 10분
    @Transactional
    public void cleanupExpiredTempChats() {
        log.debug("만료된 임시 채팅 정리 시작");

        List<TempChat> expiredChats = tempChatRepository.findExpiredTempChats(LocalDateTime.now());

        if (expiredChats.isEmpty()) {
            log.debug("만료된 임시 채팅 없음");
            return;
        }

        for (TempChat tempChat : expiredChats) {
            tempChat.expire();
            log.info("임시 채팅 자동 만료 처리 - ID: {}", tempChat.getId());
        }

        tempChatRepository.saveAll(expiredChats);

        log.info("만료된 임시 채팅 정리 완료 - 처리 수: {}", expiredChats.size());
    }

    // ===== Private Helper Methods =====

    private Profile findProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> {
                    log.error("프로필을 찾을 수 없음 - ID: {}", profileId);
                    return new IllegalArgumentException("프로필을 찾을 수 없습니다: " + profileId);
                });
    }

    private TempChat findActiveTempChatWithPermission(Long tempChatId, Long profileId) {
        return tempChatRepository.findActiveTempChatByIdAndProfileId(tempChatId, profileId)
                .orElseThrow(() -> {
                    log.error("활성 임시 채팅을 찾을 수 없거나 권한이 없음 - 채팅: {}, 프로필: {}", tempChatId, profileId);
                    return new IllegalArgumentException("임시 채팅을 찾을 수 없거나 권한이 없습니다.");
                });
    }
}

