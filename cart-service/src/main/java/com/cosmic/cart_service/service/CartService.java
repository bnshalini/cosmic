package com.cosmic.cart_service.service;

import com.cosmic.cart_service.dto.CartRequestDto;
import com.cosmic.cart_service.dto.CartResponseDto;

public interface CartService {
    CartResponseDto addToCart(CartRequestDto dto);

    CartResponseDto getCartByUserId(Long userId);

    CartResponseDto updateCartItemQuantity(Long userId, Long productId, Integer quantity);

    void removeItemFromCart(Long userId, Long productId);

    void clearCart(Long userId);
}
