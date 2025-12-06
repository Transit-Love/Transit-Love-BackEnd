package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.BalanceGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceGameRepository extends JpaRepository<BalanceGame, Long> {

    List<BalanceGame> findByIdIn(List<Long> ids);
}