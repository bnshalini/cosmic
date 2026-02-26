package com.cosmic.payment_service.dto;

import com.cosmic.payment_service.enums.PaymentStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateRequestDto {
    private PaymentStatus paymentStatus;
}