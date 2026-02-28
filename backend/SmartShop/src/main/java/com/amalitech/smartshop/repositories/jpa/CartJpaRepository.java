package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    /**
     * Load a cart by id with items and each item's product JOIN FETCHed — no N+1.
     */
    @Override
    @EntityGraph("Cart.withItemsAndProduct")
    Optional<Cart> findById(Long id);

    /** Loads a cart by user ID with items and each item's product JOIN FETCHed — no N+1. */
    @EntityGraph("Cart.withItemsAndProduct")
    Optional<Cart> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}