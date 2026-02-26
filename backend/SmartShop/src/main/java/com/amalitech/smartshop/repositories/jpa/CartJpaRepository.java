package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * I provide JPA repository operations for Cart entity.
 */
@Repository
public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    /**
     * I find a cart by user ID.
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * I find a cart by user ID with items eagerly loaded.
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * I check if a user has a cart.
     */
    boolean existsByUserId(Long userId);

    /**
     * I delete a cart by user ID.
     */
    void deleteByUserId(Long userId);
}
