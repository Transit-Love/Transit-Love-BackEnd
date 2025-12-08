package com.example.transitlove.domain.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 음소거 상태 Entity
 */
@Entity
@Table(name = "voice_mute_status",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_chat_room_profile",
           columnNames = {"chat_room_id", "profile_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoiceMuteStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Builder.Default
    @Column(name = "is_muted", nullable = false)
    private Boolean isMuted = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateMuteStatus(boolean isMuted) {
        this.isMuted = isMuted;
    }
}

