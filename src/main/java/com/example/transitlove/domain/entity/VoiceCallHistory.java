package com.example.transitlove.domain.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 음성 통화 기록 Entity
 */
@Entity
@Table(name = "voice_call_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoiceCallHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "caller_profile_id", nullable = false)
    private Long callerProfileId;

    @Column(name = "receiver_profile_id", nullable = false)
    private Long receiverProfileId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration", nullable = false)
    private Integer duration; // 초 단위

    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", nullable = false, length = 20)
    private CallStatus callStatus;

    @Builder.Default
    @Column(name = "voice_modulation_used")
    private Boolean voiceModulationUsed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

