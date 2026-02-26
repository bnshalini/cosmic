package com.cosmic.cart_service.controller;

import com.cosmic.cart_service.dto.CartRequestDto;
import com.cosmic.cart_service.dto.CartResponseDto;
import com.cosmic.cart_service.payload.ApiResponse;
import com.cosmic.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponseDto>> addToCart(
            @Valid @RequestBody CartRequestDto dto
    ) {

        CartResponseDto responseDto = cartService.addToCart(dto);

        return ResponseEntity.ok(
                ApiResponse.<CartResponseDto>builder()
                        .success(true)
                        .message("Product added to cart successfully")
                        .data(responseDto)
                        .build()
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCartByUserId(
            @PathVariable Long userId
    ) {

        CartResponseDto responseDto = cartService.getCartByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.<CartResponseDto>builder()
                        .success(true)
                        .message("Cart fetched successfully")
                        .data(responseDto)
                        .build()
        );
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateCartItemQuantity(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {

        CartResponseDto responseDto = cartService.updateCartItemQuantity(userId, productId, quantity);

        return ResponseEntity.ok(
                ApiResponse.<CartResponseDto>builder()
                        .success(true)
                        .message("Cart updated successfully")
                        .data(responseDto)
                        .build()
        );
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<Object>> removeItemFromCart(
            @RequestParam Long userId,
            @RequestParam Long productId
    ) {

        cartService.removeItemFromCart(userId, productId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Item removed from cart successfully")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<ApiResponse<Object>> clearCart(
            @PathVariable Long userId
    ) {

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Cart cleared successfully")
                        .data(null)
                        .build()
        );
    }
}
