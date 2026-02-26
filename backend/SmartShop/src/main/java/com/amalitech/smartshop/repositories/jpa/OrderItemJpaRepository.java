package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * I provide JPA repository operations for OrderItem entity.
 */
@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {

    /**
     * I find all order items for an order.
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * I delete all order items for an order.
     */
    void deleteByOrderId(Long orderId);
}
