package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 임시 채팅 엔티티 (커플이 아닌 사용자 간의 2시간 제한 채팅)
 * DDD - Aggregate Root
 */
@Entity
@Table(name = "temp_chats")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile1_id", nullable = false)
    private Profile profile1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile2_id", nullable = false)
    private Profile profile2;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusHours(2); // 2시간 후 만료
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * 임시 채팅이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 임시 채팅 종료
     */
    public void expire() {
        this.isActive = false;
    }

    /**
     * 프로필이 이 임시 채팅의 참여자인지 확인
     */
    public boolean isParticipant(Long profileId) {
        return profile1.getId().equals(profileId) || profile2.getId().equals(profileId);
    }

    /**
     * 상대방 프로필 조회
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
     * 남은 시간 계산 (분 단위)
     */
    public long getRemainingMinutes() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0;
        }
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }
}

