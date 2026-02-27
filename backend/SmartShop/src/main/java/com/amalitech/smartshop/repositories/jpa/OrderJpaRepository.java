package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * I provide JPA repository operations for Order entity.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    /**
     * I load a single order with its items, each item's product, and the user — zero extra queries.
     */
    @EntityGraph("Order.withItemsProductAndUser")
    Optional<Order> findById(Long id);

    /**
     * I load all orders (paginated) with items, item products, and users JOIN FETCHed — no N+1.
     */
    @EntityGraph("Order.withItemsProductAndUser")
    Page<Order> findAll(Pageable pageable);

    /**
     * I find all orders for a user (paginated) with items and item products JOIN FETCHed.
     */
    @EntityGraph("Order.withItemsProductAndUser")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * I find all orders for a user.
     */
    List<Order> findByUserId(Long userId);
}
