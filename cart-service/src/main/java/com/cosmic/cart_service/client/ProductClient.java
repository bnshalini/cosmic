package com.cosmic.cart_service.client;

import com.cosmic.cart_service.dto.ProductResponseDto;
import com.cosmic.cart_service.payload.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ApiResponse<ProductResponseDto> getProductById(@PathVariable("id") Long id);
}
