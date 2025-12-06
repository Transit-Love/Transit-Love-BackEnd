package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "balance_game_answers")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceGameAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_game_id", nullable = false)
    private BalanceGame balanceGame;

    @Column(nullable = false)
    private Integer selectedOption; // 1 or 2

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}