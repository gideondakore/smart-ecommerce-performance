package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.CartItem;
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

    List<CartItem> findByCart_Id(Long cartId);

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

    boolean existsByCart_IdAndProduct_Id(Long cartId, Long productId);

    /**
     * Updates quantity for a specific cart-product pair.
     * Custom JPQL is needed because derived queries cannot express UPDATE statements.
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    int updateQuantityByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    void deleteByCart_IdAndProduct_Id(Long cartId, Long productId);

    void deleteByCart_Id(Long cartId);
}
