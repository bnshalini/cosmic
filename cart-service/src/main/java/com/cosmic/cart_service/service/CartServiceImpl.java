package com.cosmic.cart_service.service;

import com.cosmic.cart_service.dto.CartRequestDto;
import com.cosmic.cart_service.dto.CartResponseDto;
import com.cosmic.cart_service.client.ProductClient;
import com.cosmic.cart_service.dto.CartItemResponseDto;
import com.cosmic.cart_service.dto.CartRequestDto;
import com.cosmic.cart_service.dto.CartResponseDto;
import com.cosmic.cart_service.dto.ProductResponseDto;
import com.cosmic.cart_service.entity.Cart;
import com.cosmic.cart_service.entity.CartItem;
import com.cosmic.cart_service.exceptions.ResourceNotFoundException;
import com.cosmic.cart_service.payload.ApiResponse;
import com.cosmic.cart_service.repository.CartItemRepository;
import com.cosmic.cart_service.repository.CartRepository;
import com.cosmic.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final ModelMapper modelMapper;

    @Override
    public CartResponseDto addToCart(CartRequestDto dto) {

        ApiResponse<ProductResponseDto> productResponse = productClient.getProductById(dto.getProductId());

        if (productResponse == null || productResponse.getData() == null) {
            throw new ResourceNotFoundException("Product not found with id: " + dto.getProductId());
        }

        ProductResponseDto product = productResponse.getData();

        // find or create cart
        Cart cart = cartRepository.findByUserId(dto.getUserId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .userId(dto.getUserId())
                                .build()
                ));

        // check if item already exists
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), dto.getProductId())
                .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
            existingItem.setPrice(product.getPrice());

            cartItemRepository.save(existingItem);

        } else {

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(dto.getProductId())
                    .quantity(dto.getQuantity())
                    .price(product.getPrice())
                    .build();

            cartItemRepository.save(newItem);
        }

        return getCartByUserId(dto.getUserId());
    }

    @Override
    public CartResponseDto getCartByUserId(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for userId: " + userId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponseDto> responseItems = cartItems.stream()
                .map(item -> {

                    ApiResponse<ProductResponseDto> productResponse =
                            productClient.getProductById(item.getProductId());

                    ProductResponseDto product = productResponse.getData();

                    // ✅ ModelMapper used here
                    CartItemResponseDto itemDto = modelMapper.map(item, CartItemResponseDto.class);

                    // extra fields from product-service
                    if (product != null) {
                        itemDto.setProductName(product.getName());
                        itemDto.setProductImage(product.getImageUrl());
                    }

                    return itemDto;
                })
                .toList();

        BigDecimal grandTotal = responseItems.stream()
                .map(CartItemResponseDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = responseItems.stream()
                .mapToInt(CartItemResponseDto::getQuantity)
                .sum();

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .items(responseItems)
                .grandTotal(grandTotal)
                .totalItems(totalItems)
                .build();
    }

    @Override
    public CartResponseDto updateCartItemQuantity(Long userId, Long productId, Integer quantity) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for userId: " + userId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found for productId: " + productId));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCartByUserId(userId);
    }

    @Override
    public void removeItemFromCart(Long userId, Long productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for userId: " + userId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found for productId: " + productId));

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for userId: " + userId));

        cartItemRepository.deleteByCartId(cart.getId());
    }
}
