package com.example.transitlove.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "balance_games")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, length = 200)
    private String option1;

    @Column(nullable = false, length = 200)
    private String option2;
}