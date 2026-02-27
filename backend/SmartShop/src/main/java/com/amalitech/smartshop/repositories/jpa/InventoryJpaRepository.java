package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * I provide JPA repository operations for Inventory entity.
 */
@Repository
public interface InventoryJpaRepository extends JpaRepository<Inventory, Long> {

    /**
     * I load a single inventory with its product JOIN FETCHed — zero extra queries.
     */
    @EntityGraph("Inventory.withProduct")
    Optional<Inventory> findById(Long id);

    /**
     * I load all inventories (paginated) with products JOIN FETCHed — no N+1.
     */
    @EntityGraph("Inventory.withProduct")
    Page<Inventory> findAll(Pageable pageable);

    /**
     * I find inventory by product ID with product JOIN FETCHed.
     */
    @EntityGraph("Inventory.withProduct")
    Optional<Inventory> findByProductId(Long productId);

    /**
     * I check if inventory exists for a product.
     */
    boolean existsByProductId(Long productId);

    /**
     * I update quantity for a product inventory.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = :quantity WHERE i.productId = :productId")
    int updateQuantityByProductId(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * I decrement quantity for a product.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity WHERE i.productId = :productId AND i.quantity >= :quantity")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * I increment quantity for a product.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :quantity WHERE i.productId = :productId")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * I delete inventory by product ID.
     */
    void deleteByProductId(Long productId);
}
