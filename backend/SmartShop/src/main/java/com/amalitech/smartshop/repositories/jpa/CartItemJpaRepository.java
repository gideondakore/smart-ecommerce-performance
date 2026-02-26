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
 * I provide JPA repository operations for CartItem entity.
 */
@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItem, Long> {

    /**
     * I find all items in a cart.
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * I find a cart item by cart ID and product ID.
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * I check if a product exists in a cart.
     */
    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    /**
     * I update quantity of a cart item.
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cartId = :cartId AND ci.productId = :productId")
    int updateQuantityByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * I delete a cart item by cart ID and product ID.
     */
    void deleteByCartIdAndProductId(Long cartId, Long productId);

    /**
     * I delete all items in a cart.
     */
    void deleteByCartId(Long cartId);
}
