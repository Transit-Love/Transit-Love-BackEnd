package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

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
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchType matchType;

    @Column(nullable = false)
    private Double similarityScore; // 유사도 점수 (0.0 ~ 1.0)

    @Column(nullable = false)
    private LocalDateTime matchedAt;

    private LocalDateTime unmatchedAt;

    public enum MatchType {
        AUTO_MATCHED,  // 자동 매칭
        MANUAL         // 수동 매칭
    }

    @PrePersist
    protected void onCreate() {
        matchedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (matchType == null) {
            matchType = MatchType.MANUAL;
        }
        if (similarityScore == null) {
            similarityScore = 0.0;
        }
    }

    public void unmatch() {
        this.isActive = false;
        this.unmatchedAt = LocalDateTime.now();
    }

    public boolean isParticipant(Long profileId) {
        return profile1.getId().equals(profileId) || profile2.getId().equals(profileId);
    }

    public Profile getOtherProfile(Long profileId) {
        if (profile1.getId().equals(profileId)) {
            return profile2;
        } else if (profile2.getId().equals(profileId)) {
            return profile1;
        }
        return null;
    }
}

