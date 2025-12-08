package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.BalanceGameAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceGameAnswerRepository extends JpaRepository<BalanceGameAnswer, Long> {

    List<BalanceGameAnswer> findByProfileId(Long profileId);
}

