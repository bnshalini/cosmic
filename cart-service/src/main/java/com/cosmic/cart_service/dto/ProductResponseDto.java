package com.cosmic.cart_service.dto;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imagePath;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
}
