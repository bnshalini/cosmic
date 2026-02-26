package com.cosmic.product_service.dto;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageResponseDto {
    private Long id;
    private String imagePath;
    private String imageUrl;
    private String thumbnailPath;
    private String thumbnailUrl;
}
