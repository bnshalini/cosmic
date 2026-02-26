package com.cosmic.payment_service.controller;

import com.cosmic.payment_service.dto.PaymentRequestDto;
import com.cosmic.payment_service.dto.PaymentVerifyRequestDto;
import com.cosmic.payment_service.dto.RazorpayOrderResponseDto;
import com.cosmic.payment_service.payload.ApiResponse;
import com.cosmic.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RazorpayOrderResponseDto>> createPaymentOrder(
            @Valid @RequestBody PaymentRequestDto dto
    ) {

        RazorpayOrderResponseDto response = paymentService.createRazorpayOrder(dto);

        return ResponseEntity.ok(
                ApiResponse.<RazorpayOrderResponseDto>builder()
                        .success(true)
                        .message("Razorpay order created successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Object>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequestDto dto
    ) {

        String message = paymentService.verifyPayment(dto);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message(message)
                        .data(null)
                        .build()
        );
    }
}