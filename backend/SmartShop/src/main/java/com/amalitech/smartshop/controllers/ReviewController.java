package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.AddReviewDTO;
import com.amalitech.smartshop.dtos.requests.UpdateReviewDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.dtos.responses.RatingDistributionDTO;
import com.amalitech.smartshop.dtos.responses.ReviewResponseDTO;
import com.amalitech.smartshop.dtos.responses.ReviewSummaryDTO;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.interfaces.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for review management operations.
 * Handles CRUD operations for product reviews.
 */
@Tag(name = "Review Management", description = "APIs for managing product reviews")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Add a new review", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(
            @Valid @RequestBody AddReviewDTO request,
            @AuthenticationPrincipal User currentUser) {
        ReviewResponseDTO review = reviewService.addReview(request, currentUser.getId());
        ApiResponse<ReviewResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review added successfully", review);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update a review", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewDTO request,
            @AuthenticationPrincipal User currentUser) {
        ReviewResponseDTO updatedReview = reviewService.updateReview(id, request, currentUser.getId());
        ApiResponse<ReviewResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review updated successfully", updatedReview);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a review", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        reviewService.deleteReview(id, currentUser.getId());
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get review by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewById(@PathVariable Long id) {
        ReviewResponseDTO review = reviewService.getReviewById(id);
        ApiResponse<ReviewResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review fetched successfully", review);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all reviews")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponseDTO>>> getAllReviews(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ReviewResponseDTO> reviews = reviewService.getAllReviews(pageable);
        PagedResponse<ReviewResponseDTO> pagedResponse = new PagedResponse<>(
                reviews.getContent(),
                reviews.getNumber(),
                (int) reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isLast()
        );
        ApiResponse<PagedResponse<ReviewResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Reviews fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get reviews by product ID")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponseDTO>>> getReviewsByProductId(
            @PathVariable Long productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByProductId(productId, pageable);
        PagedResponse<ReviewResponseDTO> pagedResponse = new PagedResponse<>(
                reviews.getContent(),
                reviews.getNumber(),
                (int) reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isLast()
        );
        ApiResponse<PagedResponse<ReviewResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Reviews fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get reviews by user", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponseDTO>>> getReviewsByUser(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByUserId(currentUser.getId(), pageable);
        PagedResponse<ReviewResponseDTO> pagedResponse = new PagedResponse<>(
                reviews.getContent(),
                reviews.getNumber(),
                (int) reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isLast()
        );
        ApiResponse<PagedResponse<ReviewResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Reviews fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get review by user and product")
    @GetMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewByUserAndProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        ReviewResponseDTO review = reviewService.getUserReviewForProduct(userId, productId);
        ApiResponse<ReviewResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review fetched successfully", review);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Check if user has reviewed a product")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> hasUserReviewed(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        boolean reviewed = reviewService.hasUserReviewedProduct(userId, productId);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review check completed", reviewed);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get average rating for a product")
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ApiResponse<List<RatingDistributionDTO>>> getRatingDistribution(@PathVariable Long productId) {
        List<RatingDistributionDTO> distribution = reviewService.getRatingDistribution(productId);
        ApiResponse<List<RatingDistributionDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Rating distribution fetched successfully", distribution);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get review summary for a product")
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryDTO>> getReviewSummary(@PathVariable Long productId) {
        ReviewSummaryDTO summary = reviewService.getReviewSummary(productId);
        ApiResponse<ReviewSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Review summary fetched successfully", summary);
        return ResponseEntity.ok(apiResponse);
    }
}
