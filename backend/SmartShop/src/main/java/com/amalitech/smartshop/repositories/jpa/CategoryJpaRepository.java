package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * I provide JPA repository operations for Category entity.
 */
@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    /**
     * Retrieves all categories with their product count using native SQL.
     * Native SQL is used because GROUP BY with COUNT across a join
     * produces a tabular result set best expressed in SQL.
     */
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT c.id, c.name, c.description, COUNT(p.id) AS product_count "
                    + "FROM categories c LEFT JOIN products p ON c.id = p.category_id "
                    + "GROUP BY c.id, c.name, c.description ORDER BY product_count DESC",
            nativeQuery = true)
    List<Object[]> findAllWithProductCount();

    /**
     * I check if a category exists with the given name (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * I find a category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
