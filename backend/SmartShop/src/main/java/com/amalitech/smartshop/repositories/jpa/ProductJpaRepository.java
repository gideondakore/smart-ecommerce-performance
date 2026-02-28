package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
     * I find a single product with its inventory and category JOIN FETCHed — zero extra queries.
     */
    @EntityGraph("Product.withInventoryAndCategory")
    Optional<Product> findById(Long id);

    /**
     * I find all products (paginated) with inventory and category JOIN FETCHed — zero extra queries per row.
     */
    @EntityGraph("Product.withInventoryAndCategory")
    Page<Product> findAll(Pageable pageable);

    /**
     * I find all products matching a specification, with inventory and category JOIN FETCHed.
     */
    @EntityGraph("Product.withInventoryAndCategory")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph("Product.withInventoryAndCategory")
    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);

    @EntityGraph("Product.withInventoryAndCategory")
    List<Product> findByCategory_Id(Long categoryId);

    @EntityGraph("Product.withInventoryAndCategory")
    Page<Product> findByVendor_Id(Long vendorId, Pageable pageable);

    /**
     * Loads all products with inventory and category in a single query.
     * JPQL JOIN FETCH is needed because @EntityGraph cannot be applied to a simple findAll returning List.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.inventory LEFT JOIN FETCH p.category LEFT JOIN FETCH p.vendor")
    List<Product> findAllWithInventory();

    /**
     * Loads products by category with inventory and category JOIN FETCHed.
     * JPQL is used with a separate countQuery to avoid in-memory pagination.
     */
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.inventory LEFT JOIN FETCH p.category LEFT JOIN FETCH p.vendor WHERE p.category.id = :categoryId",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryIdWithInventory(@Param("categoryId") Long categoryId, Pageable pageable);
}
