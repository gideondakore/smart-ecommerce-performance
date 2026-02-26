package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * I provide JPA repository operations for User entity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    /**
     * I find a user by their email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * I check if a user exists with the given email.
     */
    boolean existsByEmail(String email);
}
