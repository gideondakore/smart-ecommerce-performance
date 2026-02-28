package com.amalitech.smartshop.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BestSellerDTO {
    private Long productId;
    private String productName;
    private Long totalSold;
    private Double totalRevenue;
}
