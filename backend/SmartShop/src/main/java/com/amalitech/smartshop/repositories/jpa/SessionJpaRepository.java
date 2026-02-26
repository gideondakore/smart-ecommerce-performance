package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * I provide JPA repository operations for Session entity.
 */
@Repository
public interface SessionJpaRepository extends JpaRepository<Session, Long> {

    /**
     * I find a session by token.
     */
    Optional<Session> findByToken(String token);

    /**
     * I find a valid (non-expired) session by token.
     */
    @Query("SELECT s FROM Session s WHERE s.token = :token AND s.expiresAt > :now")
    Optional<Session> findValidSessionByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * I delete a session by token.
     */
    void deleteByToken(String token);

    /**
     * I delete all sessions for a user.
     */
    void deleteByUserId(Long userId);

    /**
     * I delete expired sessions.
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * I check if a valid session exists for a token.
     */
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.token = :token AND s.expiresAt > :now")
    boolean existsValidSession(@Param("token") String token, @Param("now") LocalDateTime now);
}
