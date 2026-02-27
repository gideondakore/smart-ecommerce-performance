package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID with items eagerly loaded
     */
    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findById(Long id);

    /**
     * Find cart by user ID with items eagerly loaded
     */
    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
}