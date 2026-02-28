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
     * Retrieves the rating distribution for a product using native SQL.
     * Native SQL is used because GROUP BY with COUNT on a single column
     * produces a clean tabular result best consumed as raw rows.
     */
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT r.rating, COUNT(r.id) AS count FROM reviews r "
                    + "WHERE r.product_id = :productId GROUP BY r.rating ORDER BY r.rating",
            nativeQuery = true)
    List<Object[]> getRatingDistribution(@org.springframework.data.repository.query.Param("productId") Long productId);

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

    @EntityGraph("Review.withProductAndUser")
    Page<Review> findByProduct_Id(Long productId, Pageable pageable);

    @EntityGraph("Review.withProductAndUser")
    Page<Review> findByUser_Id(Long userId, Pageable pageable);

    List<Review> findByProduct_Id(Long productId);

    List<Review> findByUser_Id(Long userId);

    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    /**
     * Calculates average rating for a product.
     * JPQL is needed because aggregate functions are not supported via derived queries.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);

    long countByProduct_Id(Long productId);
}
