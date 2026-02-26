package com.cosmic.payment_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponseDto {

    private Long paymentId;
    private Long orderId;
    private Long userId;

    private BigDecimal amount;
    private String currency;

    private String razorpayOrderId;

    private String razorpayKeyId;
}