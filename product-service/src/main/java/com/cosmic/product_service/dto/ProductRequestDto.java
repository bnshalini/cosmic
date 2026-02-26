package com.cosmic.product_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDto {
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotNull(message = "Price cannot be null")
    //inclusive = false means the minimum value is not allowed, so the number must be strictly greater than the given value (ex: price must be > 0.0).
    @DecimalMin(value = "0.0", inclusive = false, message = "Price should be greater than 0")
    private BigDecimal price;

    @NotNull(message = "stock cannot be null")
    @Min(value = 1, message = "stock must be at least 1")
    private Integer stock;

    @NotNull(message = "Category Id is required")
    private Long categoryId;

    @NotNull(message = "SubCategory Id is required")
    private Long subCategoryId;
}
