package com.example.transitlove.domain.repository;

import com.example.transitlove.domain.entity.HeartMessage;
import com.example.transitlove.domain.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeartMessageRepository extends JpaRepository<HeartMessage, Long> {

    @Query("SELECT hm FROM HeartMessage hm WHERE hm.senderProfile = :profile OR hm.receiverProfile = :profile ORDER BY hm.sentAt DESC")
    List<HeartMessage> findByProfile(@Param("profile") Profile profile);

    List<HeartMessage> findByReceiverProfileOrderBySentAtDesc(Profile receiverProfile);

    List<HeartMessage> findBySenderProfileOrderBySentAtDesc(Profile senderProfile);
}
