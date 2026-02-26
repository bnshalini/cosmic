package com.cosmic.payment_service.service;

import com.cosmic.payment_service.dto.PaymentRequestDto;
import com.cosmic.payment_service.dto.PaymentVerifyRequestDto;
import com.cosmic.payment_service.dto.RazorpayOrderResponseDto;

public interface PaymentService {

    RazorpayOrderResponseDto createRazorpayOrder(PaymentRequestDto dto);

    String verifyPayment(PaymentVerifyRequestDto dto);
}
