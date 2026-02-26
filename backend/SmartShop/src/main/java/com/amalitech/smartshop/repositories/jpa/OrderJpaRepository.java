package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * I provide JPA repository operations for Order entity.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    /**
     * I find all orders for a user with pagination.
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * I find all orders for a user.
     */
    List<Order> findByUserId(Long userId);
}
