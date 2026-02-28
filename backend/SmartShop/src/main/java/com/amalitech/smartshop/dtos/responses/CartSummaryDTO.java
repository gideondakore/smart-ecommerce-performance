package com.amalitech.smartshop.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartSummaryDTO {
    private Long cartId;
    private Long userId;
    private Long itemCount;
    private Long totalQuantity;
    private Double totalAmount;
}
