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

    @EntityGraph("Inventory.withProduct")
    Optional<Inventory> findByProduct_Id(Long productId);

    boolean existsByProduct_Id(Long productId);

    /**
     * Updates quantity for a product inventory.
     * JPQL is needed because derived queries cannot express UPDATE statements.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = :quantity WHERE i.product.id = :productId")
    int updateQuantityByProductId(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Atomically decrements stock only when sufficient quantity exists.
     * JPQL is required because this conditional update cannot be expressed via derived queries.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity WHERE i.product.id = :productId AND i.quantity >= :quantity")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Increments stock for a product.
     * JPQL is needed because derived queries cannot express arithmetic in UPDATE statements.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :quantity WHERE i.product.id = :productId")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    void deleteByProduct_Id(Long productId);
}
