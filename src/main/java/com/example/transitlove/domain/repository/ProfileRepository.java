package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.Profile;
import com.example.transitlove.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);

    boolean existsByUser(User user);

    Optional<Profile> findByUserId(Long userId);
}