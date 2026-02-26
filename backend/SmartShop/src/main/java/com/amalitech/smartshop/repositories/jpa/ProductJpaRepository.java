package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * I provide JPA repository operations for Product entity.
 * I extend JpaSpecificationExecutor for dynamic query support.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * I check if a product exists with the given name (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * I find all products by category ID with pagination.
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * I find all products by category ID.
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * I find all products by vendor ID with pagination.
     */
    Page<Product> findByVendorId(Long vendorId, Pageable pageable);

    /**
     * I find all products that have inventory with pagination.
     */
    @Query("SELECT p FROM Product p WHERE EXISTS (SELECT 1 FROM Inventory i WHERE i.productId = p.id)")
    Page<Product> findAllWithInventory(Pageable pageable);

    /**
     * I find all products that have inventory.
     */
    @Query("SELECT p FROM Product p WHERE EXISTS (SELECT 1 FROM Inventory i WHERE i.productId = p.id)")
    List<Product> findAllWithInventory();

    /**
     * I find products by category that have inventory with pagination.
     */
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND EXISTS (SELECT 1 FROM Inventory i WHERE i.productId = p.id)")
    Page<Product> findByCategoryIdWithInventory(@Param("categoryId") Long categoryId, Pageable pageable);
}
