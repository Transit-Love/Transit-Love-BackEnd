package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 4)
    private String mbti;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileKeyword> profileKeywords = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BalanceGameAnswer> balanceGameAnswers = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String mbti) {
        this.nickname = nickname;
        this.mbti = mbti;
    }

    public void addKeyword(ProfileKeyword profileKeyword) {
        this.profileKeywords.add(profileKeyword);
        profileKeyword.setProfile(this);
    }

    public void addBalanceGameAnswer(BalanceGameAnswer answer) {
        this.balanceGameAnswers.add(answer);
        answer.setProfile(this);
    }

    public void clearKeywords() {
        this.profileKeywords.clear();
    }

    public void clearBalanceGameAnswers() {
        this.balanceGameAnswers.clear();
    }
}
