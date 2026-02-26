package com.cosmic.payment_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private String paymentStatus;
}