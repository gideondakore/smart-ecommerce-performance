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
     * Finds a non-expired session by token.
     * JPQL is needed because derived queries cannot express temporal comparisons against a parameter.
     */
    @Query("SELECT s FROM Session s WHERE s.token = :token AND s.expiresAt > :now")
    Optional<Session> findValidSessionByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    void deleteByToken(String token);

    void deleteByUser_Id(Long userId);

    /**
     * Removes all expired sessions in a single bulk DELETE.
     * JPQL is needed because derived queries cannot express temporal comparisons against a parameter.
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Checks whether a valid (non-expired) session exists for a given token.
     * JPQL is needed because derived queries cannot express temporal comparisons against a parameter.
     */
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.token = :token AND s.expiresAt > :now")
    boolean existsValidSession(@Param("token") String token, @Param("now") LocalDateTime now);
}
