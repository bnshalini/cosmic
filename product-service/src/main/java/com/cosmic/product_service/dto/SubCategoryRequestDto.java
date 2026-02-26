package com.cosmic.product_service.dto;

import lombok.Data;

@Data
public class SubCategoryRequestDto {
    private String name;
    private Long categoryId;
}
