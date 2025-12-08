package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 두 프로필 간의 채팅방 조회 (프로필 순서 무관)
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.profile1.id = :profileId1 AND cr.profile2.id = :profileId2) OR " +
            "(cr.profile1.id = :profileId2 AND cr.profile2.id = :profileId1)")
    Optional<ChatRoom> findByProfileIds(@Param("profileId1") Long profileId1,
                                         @Param("profileId2") Long profileId2);

    /**
     * 특정 프로필이 참여한 모든 활성 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.profile1.id = :profileId OR cr.profile2.id = :profileId) " +
            "AND cr.isActive = true " +
            "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findActiveRoomsByProfileId(@Param("profileId") Long profileId);

    /**
     * 특정 프로필이 참여한 매칭된 채팅방만 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.profile1.id = :profileId OR cr.profile2.id = :profileId) " +
            "AND cr.isActive = true " +
            "AND cr.roomType = 'MATCHED' " +
            "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findMatchedRoomsByProfileId(@Param("profileId") Long profileId);

    /**
     * 특정 프로필이 참여한 매칭되지 않은 채팅방만 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.profile1.id = :profileId OR cr.profile2.id = :profileId) " +
            "AND cr.isActive = true " +
            "AND cr.roomType = 'UNMATCHED' " +
            "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findUnmatchedRoomsByProfileId(@Param("profileId") Long profileId);

    /**
     * 어드민용 - 모든 매칭된 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = 'MATCHED' AND cr.isActive = true " +
            "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findAllMatchedRooms();
}

