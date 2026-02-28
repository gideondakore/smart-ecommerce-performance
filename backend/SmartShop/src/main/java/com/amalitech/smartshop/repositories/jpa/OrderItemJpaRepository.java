package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * I provide JPA repository operations for OrderItem entity.
 */
@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Finds the best-selling products by total quantity sold using native SQL.
     * Native SQL is needed because this aggregate GROUP BY with SUM across
     * a join and ORDER BY cannot be expressed via derived queries.
     */
    @Query(value = "SELECT p.id AS product_id, p.name AS product_name, "
            + "SUM(oi.quantity) AS total_sold, SUM(oi.total_price) AS total_revenue "
            + "FROM order_items oi JOIN products p ON oi.product_id = p.id "
            + "GROUP BY p.id, p.name ORDER BY total_sold DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findBestSellingProducts(@Param("limit") Integer limit);

    List<OrderItem> findByOrder_Id(Long orderId);

    void deleteByOrder_Id(Long orderId);
}
