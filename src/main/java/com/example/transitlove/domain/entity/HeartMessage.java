package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "heart_messages")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_profile_id", nullable = false)
    private Profile senderProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_profile_id", nullable = false)
    private Profile receiverProfile;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
