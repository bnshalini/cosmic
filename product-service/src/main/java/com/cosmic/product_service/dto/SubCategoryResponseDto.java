package com.cosmic.product_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubCategoryResponseDto {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
}
