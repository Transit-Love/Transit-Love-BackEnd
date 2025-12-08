package com.example.transitlove.domain.service;

import com.example.transitlove.domain.entity.CallStatus;
import com.example.transitlove.domain.entity.VoiceCallHistory;
import com.example.transitlove.domain.entity.VoiceMuteStatus;
import com.example.transitlove.domain.repository.VoiceCallHistoryRepository;
import com.example.transitlove.domain.repository.VoiceMuteStatusRepository;
import com.example.transitlove.domain.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 음성 통화 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VoiceCallManagementService {

    private final VoiceCallHistoryRepository voiceCallHistoryRepository;
    private final VoiceMuteStatusRepository voiceMuteStatusRepository;

    /**
     * 음소거 상태 업데이트
     */
    @Transactional
    public VoiceMuteResponse updateMuteStatus(Long chatRoomId, VoiceMuteRequest request) {
        log.info("음소거 상태 업데이트 - 채팅방: {}, 프로필: {}, 음소거: {}",
                chatRoomId, request.getProfileId(), request.getIsMuted());

        VoiceMuteStatus muteStatus = voiceMuteStatusRepository
                .findByChatRoomIdAndProfileId(chatRoomId, request.getProfileId())
                .orElse(VoiceMuteStatus.builder()
                        .chatRoomId(chatRoomId)
                        .profileId(request.getProfileId())
                        .isMuted(false)
                        .build());

        muteStatus.updateMuteStatus(request.getIsMuted());
        VoiceMuteStatus saved = voiceMuteStatusRepository.save(muteStatus);

        return VoiceMuteResponse.builder()
                .chatRoomId(saved.getChatRoomId())
                .profileId(saved.getProfileId())
                .isMuted(saved.getIsMuted())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    /**
     * 통화 기록 저장
     */
    @Transactional
    public VoiceCallHistory saveCallHistory(VoiceCallHistoryRequest request) {
        log.info("통화 기록 저장 - 채팅방: {}, 발신자: {}, 수신자: {}, 시간: {}초",
                request.getChatRoomId(), request.getCallerProfileId(),
                request.getReceiverProfileId(), request.getDuration());

        // 시간 검증
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("통화 시작 시각이 종료 시각보다 늦습니다.");
        }

        VoiceCallHistory history = VoiceCallHistory.builder()
                .chatRoomId(request.getChatRoomId())
                .callerProfileId(request.getCallerProfileId())
                .receiverProfileId(request.getReceiverProfileId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .duration(request.getDuration())
                .callStatus(request.getCallStatus())
                .voiceModulationUsed(request.getVoiceModulationUsed())
                .build();

        return voiceCallHistoryRepository.save(history);
    }

    /**
     * 채팅방별 통화 기록 조회
     */
    public Page<VoiceCallHistory> getCallHistoryByChatRoom(Long chatRoomId, Pageable pageable) {
        log.info("채팅방 통화 기록 조회 - 채팅방: {}", chatRoomId);
        return voiceCallHistoryRepository.findByChatRoomIdOrderByStartTimeDesc(chatRoomId, pageable);
    }

    /**
     * 내 통화 기록 조회
     */
    public Page<VoiceCallHistory> getMyCallHistory(Long profileId, Pageable pageable) {
        log.info("프로필 통화 기록 조회 - 프로필: {}", profileId);
        return voiceCallHistoryRepository.findByProfileId(profileId, pageable);
    }

    /**
     * 통화 통계 조회
     */
    public VoiceCallStatisticsResponse getCallStatistics(Long chatRoomId) {
        log.info("통화 통계 조회 - 채팅방: {}", chatRoomId);

        List<VoiceCallHistory> histories = voiceCallHistoryRepository.findByChatRoomId(chatRoomId);

        if (histories.isEmpty()) {
            return VoiceCallStatisticsResponse.builder()
                    .chatRoomId(chatRoomId)
                    .totalCalls(0)
                    .completedCalls(0)
                    .rejectedCalls(0)
                    .missedCalls(0)
                    .failedCalls(0)
                    .totalDuration(0)
                    .averageDuration(0)
                    .longestCallDuration(0)
                    .shortestCallDuration(0)
                    .voiceModulationUsageRate(0.0)
                    .build();
        }

        Long totalCalls = voiceCallHistoryRepository.countByChatRoomId(chatRoomId);
        Long completedCalls = voiceCallHistoryRepository.countByChatRoomIdAndCallStatus(chatRoomId, CallStatus.COMPLETED);
        Long rejectedCalls = voiceCallHistoryRepository.countByChatRoomIdAndCallStatus(chatRoomId, CallStatus.REJECTED);
        Long missedCalls = voiceCallHistoryRepository.countByChatRoomIdAndCallStatus(chatRoomId, CallStatus.MISSED);
        Long failedCalls = voiceCallHistoryRepository.countByChatRoomIdAndCallStatus(chatRoomId, CallStatus.FAILED);

        Integer totalDuration = voiceCallHistoryRepository.sumDurationByChatRoomId(chatRoomId);
        if (totalDuration == null) totalDuration = 0;

        Double avgDuration = voiceCallHistoryRepository.avgDurationByChatRoomId(chatRoomId);
        if (avgDuration == null) avgDuration = 0.0;

        Integer maxDuration = voiceCallHistoryRepository.maxDurationByChatRoomId(chatRoomId);
        if (maxDuration == null) maxDuration = 0;

        Integer minDuration = voiceCallHistoryRepository.minDurationByChatRoomId(chatRoomId);
        if (minDuration == null) minDuration = 0;

        Long modulationUsed = voiceCallHistoryRepository.countByChatRoomIdAndVoiceModulationUsedTrue(chatRoomId);
        Double modulationRate = totalCalls > 0 ? (double) modulationUsed / totalCalls : 0.0;

        return VoiceCallStatisticsResponse.builder()
                .chatRoomId(chatRoomId)
                .totalCalls(totalCalls.intValue())
                .completedCalls(completedCalls.intValue())
                .rejectedCalls(rejectedCalls.intValue())
                .missedCalls(missedCalls.intValue())
                .failedCalls(failedCalls.intValue())
                .totalDuration(totalDuration)
                .averageDuration(avgDuration.intValue())
                .longestCallDuration(maxDuration)
                .shortestCallDuration(minDuration)
                .voiceModulationUsageRate(modulationRate)
                .firstCallTime(histories.stream()
                        .min(Comparator.comparing(VoiceCallHistory::getStartTime))
                        .map(VoiceCallHistory::getStartTime)
                        .orElse(null))
                .lastCallTime(histories.stream()
                        .max(Comparator.comparing(VoiceCallHistory::getStartTime))
                        .map(VoiceCallHistory::getStartTime)
                        .orElse(null))
                .build();
    }

    /**
     * 음소거 상태 조회
     */
    public VoiceMuteResponse getMuteStatus(Long chatRoomId, Long profileId) {
        log.info("음소거 상태 조회 - 채팅방: {}, 프로필: {}", chatRoomId, profileId);

        VoiceMuteStatus muteStatus = voiceMuteStatusRepository
                .findByChatRoomIdAndProfileId(chatRoomId, profileId)
                .orElse(VoiceMuteStatus.builder()
                        .chatRoomId(chatRoomId)
                        .profileId(profileId)
                        .isMuted(false)
                        .build());

        return VoiceMuteResponse.builder()
                .chatRoomId(muteStatus.getChatRoomId())
                .profileId(muteStatus.getProfileId())
                .isMuted(muteStatus.getIsMuted())
                .updatedAt(muteStatus.getUpdatedAt())
                .build();
    }
}

