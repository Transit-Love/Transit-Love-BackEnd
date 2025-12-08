package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.VoiceMuteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 음소거 상태 Repository
 */
@Repository
public interface VoiceMuteStatusRepository extends JpaRepository<VoiceMuteStatus, Long> {

    /**
     * 채팅방과 프로필로 음소거 상태 조회
     */
    Optional<VoiceMuteStatus> findByChatRoomIdAndProfileId(Long chatRoomId, Long profileId);

    /**
     * 채팅방과 프로필로 음소거 상태 존재 여부 확인
     */
    boolean existsByChatRoomIdAndProfileId(Long chatRoomId, Long profileId);

    /**
     * 채팅방별 모든 음소거 상태 삭제
     */
    void deleteByChatRoomId(Long chatRoomId);
}

