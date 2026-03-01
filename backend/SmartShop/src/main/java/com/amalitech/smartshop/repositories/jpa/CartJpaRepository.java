package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    /**
     * Calculates cart total amount using native SQL join across carts, cart_items, and products.
     * Native SQL is needed because this aggregate join across three tables with GROUP BY
     * is more naturally expressed in SQL than JPQL.
     */
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT c.id, c.user_id, COALESCE(SUM(ci.quantity * p.price), 0) AS total_amount "
                    + "FROM cart c LEFT JOIN cart_items ci ON c.id = ci.cart_id "
                    + "LEFT JOIN products p ON ci.product_id = p.id "
                    + "WHERE c.user_id = :userId GROUP BY c.id, c.user_id",
            nativeQuery = true)
    List<Object[]> getCartSummaryByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    /**
     * Load a cart by id with items and each item's product JOIN FETCHed — no N+1.
     */
    @Override
    @EntityGraph("Cart.withItemsAndProduct")
    Optional<Cart> findById(Long id);

    /** Loads a cart by user ID with items and each item's product JOIN FETCHed — no N+1. */
    @EntityGraph("Cart.withItemsAndProduct")
    Optional<Cart> findByUser_Id(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}