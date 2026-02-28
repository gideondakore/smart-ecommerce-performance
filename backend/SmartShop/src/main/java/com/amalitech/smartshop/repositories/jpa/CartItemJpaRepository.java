package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides JPA repository operations for CartItem entity.
 */
@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItem, Long> {

    /**
     * Retrieves cart item count and total quantity using native SQL.
     * Native SQL is used because aggregate functions with multiple return columns
     * cannot be expressed via derived queries or simple JPQL projections.
     */
    @Query(value = "SELECT COUNT(*) AS item_count, COALESCE(SUM(ci.quantity), 0) AS total_quantity "
            + "FROM cart_items ci WHERE ci.cart_id = :cartId", nativeQuery = true)
    List<Object[]> getCartItemSummary(@Param("cartId") Long cartId);

    @EntityGraph(attributePaths = {"product"})
    List<CartItem> findByCart_Id(Long cartId);

    @EntityGraph(attributePaths = {"product", "cart"})
    Optional<CartItem> findByCartIdAndProduct_Id(Long cartId, Long productId);

    boolean existsByCartIdAndProduct_Id(Long cartId, Long productId);

    /**
     * Updates quantity for a specific cart-product pair.
     * Custom JPQL is needed because derived queries cannot express UPDATE statements.
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    int updateQuantityByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    void deleteByCartIdAndProduct_Id(Long cartId, Long productId);

    void deleteByCart_Id(Long cartId);
}
