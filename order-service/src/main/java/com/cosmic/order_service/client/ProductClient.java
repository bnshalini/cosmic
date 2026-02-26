package com.cosmic.order_service.client;

import com.cosmic.order_service.dto.ProductResponseDto;
import com.cosmic.order_service.payload.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ApiResponse<ProductResponseDto> getProductById(@PathVariable Long id);

    @PostMapping("/internal/products/{productId}/reserve")
    ApiResponse<Void> reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity
    );

    @PostMapping("/internal/products/{productId}/confirm")
    ApiResponse<Void> confirmStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity
    );

    @PostMapping("/internal/products/{productId}/release")
    ApiResponse<Void> releaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity
    );
}
