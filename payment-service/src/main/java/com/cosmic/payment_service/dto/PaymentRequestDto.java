package com.cosmic.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotNull(message = "OrderId is required")
    private Long orderId;

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
}
