package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * I provide JPA repository operations for Review entity.
 */
@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    /**
     * I find all reviews for a product.
     */
    List<Review> findByProductId(Long productId);

    /**
     * I find all reviews for a product with pagination.
     */
    Page<Review> findByProductId(Long productId, Pageable pageable);

    /**
     * I find all reviews by a user.
     */
    List<Review> findByUserId(Long userId);

    /**
     * I find all reviews by a user with pagination.
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * I find a review by user and product.
     */
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * I check if a user has reviewed a product.
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * I calculate average rating for a product.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * I count reviews for a product.
     */
    long countByProductId(Long productId);
}
