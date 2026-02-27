package com.amalitech.smartshop.repositories.jpa;

import com.amalitech.smartshop.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * I load a single review with its product and user JOIN FETCHed — zero extra queries.
     */
    @EntityGraph("Review.withProductAndUser")
    Optional<Review> findById(Long id);

    /**
     * I load all reviews (paginated) with product and user JOIN FETCHed — no N+1.
     */
    @EntityGraph("Review.withProductAndUser")
    Page<Review> findAll(Pageable pageable);

    /**
     * I find all reviews for a product (paginated) with product and user JOIN FETCHed.
     */
    @EntityGraph("Review.withProductAndUser")
    Page<Review> findByProductId(Long productId, Pageable pageable);

    /**
     * I find all reviews by a user (paginated) with product and user JOIN FETCHed.
     */
    @EntityGraph("Review.withProductAndUser")
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * I find all reviews for a product.
     */
    List<Review> findByProductId(Long productId);

    /**
     * I find all reviews by a user.
     */
    List<Review> findByUserId(Long userId);

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
