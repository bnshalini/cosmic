package com.cosmic.order_service.client;

import com.cosmic.order_service.dto.CartResponseDto;
import com.cosmic.order_service.payload.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/api/cart/{userId}")
    ApiResponse<CartResponseDto> getCartByUserId(@PathVariable("userId") Long userId);

    @DeleteMapping("/api/cart/clear/{userId}")
    ApiResponse<Object> clearCart(@PathVariable("userId") Long userId);
}
