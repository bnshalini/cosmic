package com.cosmic.cart_service.dto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {
    private Long cartId;

    private Long userId;

    private List<CartItemResponseDto> items;

    private BigDecimal grandTotal;

    private Integer totalItems;
}
