package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.TempChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 임시 채팅 메시지 리포지토리
 */
@Repository
public interface TempChatMessageRepository extends JpaRepository<TempChatMessage, Long> {

    List<TempChatMessage> findByTempChatIdOrderBySentAtAsc(Long tempChatId);

    @Query("SELECT tcm FROM TempChatMessage tcm WHERE tcm.tempChat.id = :tempChatId AND tcm.isRead = false AND tcm.senderProfile.id != :profileId")
    List<TempChatMessage> findUnreadMessagesByTempChatIdAndNotSender(@Param("tempChatId") Long tempChatId, @Param("profileId") Long profileId);

    @Modifying
    @Query("UPDATE TempChatMessage tcm SET tcm.isRead = true, tcm.readAt = CURRENT_TIMESTAMP WHERE tcm.tempChat.id = :tempChatId AND tcm.senderProfile.id != :profileId AND tcm.isRead = false")
    void markAllAsReadByTempChatIdAndNotSender(@Param("tempChatId") Long tempChatId, @Param("profileId") Long profileId);

    @Query("SELECT COUNT(tcm) FROM TempChatMessage tcm WHERE tcm.tempChat.id = :tempChatId AND tcm.isRead = false AND tcm.senderProfile.id != :profileId")
    Long countUnreadMessagesByTempChatIdAndNotSender(@Param("tempChatId") Long tempChatId, @Param("profileId") Long profileId);

    @Query("SELECT tcm FROM TempChatMessage tcm WHERE tcm.tempChat.id = :tempChatId ORDER BY tcm.sentAt DESC LIMIT 1")
    TempChatMessage findLatestMessageByTempChatId(@Param("tempChatId") Long tempChatId);
}

