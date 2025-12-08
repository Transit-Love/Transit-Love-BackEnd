package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.VoiceCallHistory;
import com.example.transitlove.domain.entity.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 음성 통화 기록 Repository
 */
@Repository
public interface VoiceCallHistoryRepository extends JpaRepository<VoiceCallHistory, Long> {

    /**
     * 채팅방별 통화 기록 조회 (페이징)
     */
    Page<VoiceCallHistory> findByChatRoomIdOrderByStartTimeDesc(Long chatRoomId, Pageable pageable);

    /**
     * 프로필 ID로 통화 기록 조회 (발신자 또는 수신자)
     */
    @Query("SELECT v FROM VoiceCallHistory v WHERE v.callerProfileId = :profileId OR v.receiverProfileId = :profileId ORDER BY v.startTime DESC")
    Page<VoiceCallHistory> findByProfileId(@Param("profileId") Long profileId, Pageable pageable);

    /**
     * 채팅방별 총 통화 횟수
     */
    Long countByChatRoomId(Long chatRoomId);

    /**
     * 채팅방별 통화 상태별 카운트
     */
    Long countByChatRoomIdAndCallStatus(Long chatRoomId, CallStatus callStatus);

    /**
     * 채팅방별 총 통화 시간
     */
    @Query("SELECT SUM(v.duration) FROM VoiceCallHistory v WHERE v.chatRoomId = :chatRoomId")
    Integer sumDurationByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 채팅방별 평균 통화 시간
     */
    @Query("SELECT AVG(v.duration) FROM VoiceCallHistory v WHERE v.chatRoomId = :chatRoomId")
    Double avgDurationByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 채팅방별 최장 통화 시간
     */
    @Query("SELECT MAX(v.duration) FROM VoiceCallHistory v WHERE v.chatRoomId = :chatRoomId")
    Integer maxDurationByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 채팅방별 최단 통화 시간
     */
    @Query("SELECT MIN(v.duration) FROM VoiceCallHistory v WHERE v.chatRoomId = :chatRoomId")
    Integer minDurationByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 채팅방별 음성 변조 사용 횟수
     */
    Long countByChatRoomIdAndVoiceModulationUsedTrue(Long chatRoomId);

    /**
     * 채팅방별 통화 기록 전체 조회 (통계용)
     */
    List<VoiceCallHistory> findByChatRoomId(Long chatRoomId);
}

