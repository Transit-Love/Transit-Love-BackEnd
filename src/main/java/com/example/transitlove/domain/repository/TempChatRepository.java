package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.TempChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 임시 채팅 리포지토리
 * DDD - Repository Interface
 */
@Repository
public interface TempChatRepository extends JpaRepository<TempChat, Long> {

    @Query("SELECT tc FROM TempChat tc WHERE tc.isActive = true AND (tc.profile1.id = :profileId OR tc.profile2.id = :profileId)")
    List<TempChat> findActiveTempChatsByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT tc FROM TempChat tc WHERE tc.isActive = true AND tc.id = :tempChatId AND (tc.profile1.id = :profileId OR tc.profile2.id = :profileId)")
    Optional<TempChat> findActiveTempChatByIdAndProfileId(@Param("tempChatId") Long tempChatId, @Param("profileId") Long profileId);

    @Query("SELECT tc FROM TempChat tc WHERE tc.isActive = true AND ((tc.profile1.id = :profile1Id AND tc.profile2.id = :profile2Id) OR (tc.profile1.id = :profile2Id AND tc.profile2.id = :profile1Id))")
    Optional<TempChat> findActiveTempChatBetweenProfiles(@Param("profile1Id") Long profile1Id, @Param("profile2Id") Long profile2Id);

    @Query("SELECT tc FROM TempChat tc WHERE tc.isActive = true AND tc.expiresAt < :now")
    List<TempChat> findExpiredTempChats(@Param("now") LocalDateTime now);
}

