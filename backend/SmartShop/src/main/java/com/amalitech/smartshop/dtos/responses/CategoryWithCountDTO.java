package com.amalitech.smartshop.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryWithCountDTO {
    private Long id;
    private String name;
    private String description;
    private Long productCount;
}
