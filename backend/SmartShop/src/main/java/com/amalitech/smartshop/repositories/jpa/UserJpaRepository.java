package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * I provide JPA repository operations for User entity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    /**
     * Counts users grouped by role using native SQL.
     * Native SQL is used because GROUP BY on an enum column
     * with COUNT is a reporting query best expressed in raw SQL.
     */
    @Query(value = "SELECT role, COUNT(id) AS user_count FROM users GROUP BY role", nativeQuery = true)
    List<Object[]> getUserCountByRole();

    /**
     * I find a user by their email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * I check if a user exists with the given email.
     */
    boolean existsByEmail(String email);
}
