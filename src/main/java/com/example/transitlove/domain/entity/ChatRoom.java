package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 엔티티
 * 매칭 여부와 관계없이 두 사용자 간의 채팅방을 나타냄
 */
@Entity
@Table(name = "chat_rooms",
       uniqueConstraints = @UniqueConstraint(columnNames = {"profile1_id", "profile2_id"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile1_id", nullable = false)
    private Profile profile1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile2_id", nullable = false)
    private Profile profile2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match; // 매칭이 있으면 연결, 없으면 null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    private LocalDateTime expiresAt; // 만료 시간 (UNMATCHED만 해당, 생성 시간 + 2시간)

    public enum RoomType {
        MATCHED,    // 매칭된 채팅방
        UNMATCHED   // 매칭되지 않은 채팅방
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (roomType == null) {
            roomType = RoomType.UNMATCHED;
        }
        // UNMATCHED 타입이면 2시간 후 만료
        if (roomType == RoomType.UNMATCHED) {
            expiresAt = createdAt.plusHours(2);
        }
    }

    /**
     * 해당 프로필이 채팅방 참여자인지 확인
     */
    public boolean isParticipant(Long profileId) {
        return profile1.getId().equals(profileId) || profile2.getId().equals(profileId);
    }

    /**
     * 상대방 프로필 가져오기
     */
    public Profile getOtherProfile(Long profileId) {
        if (profile1.getId().equals(profileId)) {
            return profile2;
        } else if (profile2.getId().equals(profileId)) {
            return profile1;
        }
        return null;
    }

    /**
     * 마지막 메시지 시간 업데이트
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * 매칭 정보 설정
     */
    public void setMatch(Match match) {
        this.match = match;
        this.roomType = RoomType.MATCHED;
        this.expiresAt = null; // MATCHED가 되면 만료 시간 제거
    }

    /**
     * 채팅방이 만료되었는지 확인
     */
    public boolean isExpired() {
        if (roomType == RoomType.MATCHED || expiresAt == null) {
            return false; // MATCHED는 만료되지 않음
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 채팅방 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}

