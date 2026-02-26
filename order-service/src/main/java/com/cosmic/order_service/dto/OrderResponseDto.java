package com.cosmic.order_service.dto;

import com.cosmic.order_service.enums.OrderStatus;
import com.cosmic.order_service.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;
    private Long userId;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDto> items;
    private LocalDateTime createdAt;
}