package com.amalitech.smartshop.services;

import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;


/**
 * I provide JPA Specifications for dynamic Product queries.
 * Using Specifications allows building complex queries programmatically.
 */
public class ProductSpecification {

    private ProductSpecification() {
        // Utility class
    }

    /**
     * I create a specification that filters products by search term.
     * Searches in name and description fields.
     */
    public static Specification<Product> hasSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * I create a specification that filters products by category ID.
     */
    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * I create a specification that filters products by category name.
     */
    public static Specification<Product> hasCategoryName(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            if (categoryName == null || categoryName.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(categoryJoin.get("name")),
                    categoryName.toLowerCase()
            );
        };
    }

    /**
     * I create a specification that filters products by minimum price.
     */
    public static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    /**
     * I create a specification that filters products by maximum price.
     */
    public static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    /**
     * I create a specification that filters products by vendor ID.
     */
    public static Specification<Product> hasVendorId(Long vendorId) {
        return (root, query, criteriaBuilder) -> {
            if (vendorId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("vendor").get("id"), vendorId);
        };
    }

    /**
     * I create a specification that filters products by featured status.
     */
    public static Specification<Product> isFeatured(Boolean featured) {
        return (root, query, criteriaBuilder) -> {
            if (featured == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("featured"), featured);
        };
    }

    /**
     * I create a specification that filters products by active status.
     */
    public static Specification<Product> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    /**
     * I create a specification that filters products with stock available.
     */
    public static Specification<Product> hasStock() {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> inventoryJoin = root.join("inventory", JoinType.LEFT);
            return criteriaBuilder.greaterThan(inventoryJoin.get("quantity"), 0);
        };
    }

    /**
     * I build a combined specification from multiple filter parameters.
     * This is the main entry point for product search.
     */
    public static Specification<Product> buildSpecification(
            String searchTerm,
            Long categoryId,
            String categoryName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long vendorId,
            Boolean featured,
            Boolean active,
            Boolean inStock
    ) {
        return Specification.where(hasSearchTerm(searchTerm))
                .and(hasCategoryId(categoryId))
                .and(hasCategoryName(categoryName))
                .and(hasMinPrice(minPrice))
                .and(hasMaxPrice(maxPrice))
                .and(hasVendorId(vendorId))
                .and(isFeatured(featured))
                .and(isActive(active))
                .and(inStock != null && inStock ? hasStock() : (root, query, cb) -> cb.conjunction());
    }
}
