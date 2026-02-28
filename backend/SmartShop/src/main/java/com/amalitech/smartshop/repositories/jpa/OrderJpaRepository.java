package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides JPA repository operations for Order entity.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    @EntityGraph("Order.withItemsProductAndUser")
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"items", "user", "items.product"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph("Order.withItemsProductAndUser")
    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findByUser_Id(Long userId);

    /**
     * Finds high-value orders above a given total amount, eagerly loading user and items.
     * JPQL is required because derived queries cannot express greater-than on totalAmount
     * while also applying an entity graph for eager association loading.
     */
    @EntityGraph("Order.withItemsProductAndUser")
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount ORDER BY o.totalAmount DESC")
    Page<Order> findHighValueOrders(@Param("minAmount") Double minAmount, Pageable pageable);
}
