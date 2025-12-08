package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByMatchIdOrderBySentAtAsc(Long matchId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.match.id = :matchId AND cm.isRead = false AND cm.senderProfile.id != :profileId")
    List<ChatMessage> findUnreadMessagesByMatchIdAndNotSender(@Param("matchId") Long matchId, @Param("profileId") Long profileId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true, cm.readAt = CURRENT_TIMESTAMP WHERE cm.match.id = :matchId AND cm.senderProfile.id != :profileId AND cm.isRead = false")
    void markAllAsReadByMatchIdAndNotSender(@Param("matchId") Long matchId, @Param("profileId") Long profileId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.match.id = :matchId AND cm.isRead = false AND cm.senderProfile.id != :profileId")
    Long countUnreadMessagesByMatchIdAndNotSender(@Param("matchId") Long matchId, @Param("profileId") Long profileId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.match.id = :matchId ORDER BY cm.sentAt DESC LIMIT 1")
    ChatMessage findLatestMessageByMatchId(@Param("matchId") Long matchId);

    // Admin methods
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.match.id = :matchId")
    Long countByMatchId(@Param("matchId") Long matchId);

    // ChatRoom based queries
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.sentAt DESC LIMIT 1")
    ChatMessage findLatestMessageByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId AND cm.isRead = false AND cm.senderProfile.id != :profileId")
    Long countUnreadMessagesByChatRoomIdAndNotSender(@Param("chatRoomId") Long chatRoomId, @Param("profileId") Long profileId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true, cm.readAt = CURRENT_TIMESTAMP WHERE cm.chatRoom.id = :chatRoomId AND cm.senderProfile.id != :profileId AND cm.isRead = false")
    void markAllAsReadByChatRoomIdAndNotSender(@Param("chatRoomId") Long chatRoomId, @Param("profileId") Long profileId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId")
    Long countByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}

