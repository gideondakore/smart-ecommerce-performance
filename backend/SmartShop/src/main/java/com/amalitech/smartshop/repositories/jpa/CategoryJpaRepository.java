package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * I provide JPA repository operations for Category entity.
 */
@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    /**
     * I check if a category exists with the given name (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * I find a category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
