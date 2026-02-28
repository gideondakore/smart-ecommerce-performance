package com.amalitech.smartshop.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSummaryDTO {
    private Long productId;
    private Double averageRating;
    private Long reviewCount;
}
