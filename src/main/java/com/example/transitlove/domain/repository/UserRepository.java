package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySchoolEmail(String schoolEmail);

    Boolean existsBySchoolEmail(String schoolEmail);
}