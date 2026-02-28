package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddReviewDTO;
import com.amalitech.smartshop.dtos.requests.UpdateReviewDTO;
import com.amalitech.smartshop.dtos.responses.ReviewResponseDTO;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.entities.Review;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.interfaces.ReviewService;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ReviewJpaRepository;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewJpaRepository reviewRepository;
    private final ProductJpaRepository productRepository;
    private final UserJpaRepository userRepository;

    @Override
    @Transactional
    public ReviewResponseDTO addReview(AddReviewDTO request, Long userId) {
        log.info("Adding review for product: {} by user: {}", request.getProductId(), userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        log.info("Review added successfully with id: {}", savedReview.getId());
        return mapToResponseDTO(savedReview, product.getName(), user.getFullName());
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long id, UpdateReviewDTO request, Long userId) {
        log.info("Updating review: {} by user: {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own reviews");
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);

        log.info("Review updated successfully: {}", id);
        return mapToResponseDTOFromEntity(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId) {
        log.info("Deleting review: {} by user: {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.deleteById(id);
        log.info("Review deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));
        return mapToResponseDTOFromEntity(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(this::mapToResponseDTOFromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByProductId(Long productId, Pageable pageable) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return reviewRepository.findByProduct_Id(productId, pageable).map(this::mapToResponseDTOFromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return reviewRepository.findByUser_Id(userId, pageable).map(this::mapToResponseDTOFromEntity);
    }

    private ReviewResponseDTO mapToResponseDTO(Review review, String productName, String userName) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(productName)
                .userId(review.getUser().getId())
                .userName(userName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReviewResponseDTO mapToResponseDTOFromEntity(Review review) {
        String productName = review.getProduct() != null ? review.getProduct().getName() : "Unknown Product";
        String userName = review.getUser() != null ? review.getUser().getFullName() : "Unknown User";
        return mapToResponseDTO(review, productName, userName);
    }
}
