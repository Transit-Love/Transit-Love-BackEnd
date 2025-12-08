package com.example.transitlove.domain.repository;
import com.example.transitlove.domain.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.isActive = true AND (m.profile1.id = :profileId OR m.profile2.id = :profileId)")
    List<Match> findActiveMatchesByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT m FROM Match m WHERE m.isActive = true AND m.id = :matchId AND (m.profile1.id = :profileId OR m.profile2.id = :profileId)")
    Optional<Match> findActiveMatchByIdAndProfileId(@Param("matchId") Long matchId, @Param("profileId") Long profileId);

    @Query("SELECT m FROM Match m WHERE m.isActive = true AND ((m.profile1.id = :profile1Id AND m.profile2.id = :profile2Id) OR (m.profile1.id = :profile2Id AND m.profile2.id = :profile1Id))")
    Optional<Match> findActiveMatchBetweenProfiles(@Param("profile1Id") Long profile1Id, @Param("profile2Id") Long profile2Id);

    // Admin methods
    List<Match> findByIsActiveTrue();
}
