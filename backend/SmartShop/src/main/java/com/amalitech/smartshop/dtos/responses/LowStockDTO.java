package com.amalitech.smartshop.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LowStockDTO {
    private Long inventoryId;
    private Integer quantity;
    private String location;
    private Long productId;
    private String productName;
}
