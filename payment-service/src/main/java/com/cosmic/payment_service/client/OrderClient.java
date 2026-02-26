package com.cosmic.payment_service.client;

import com.cosmic.payment_service.dto.OrderResponseDto;
import com.cosmic.payment_service.dto.PaymentStatusUpdateRequestDto;
import com.cosmic.payment_service.payload.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{orderId}")
    ApiResponse<OrderResponseDto> getOrderById(@PathVariable("orderId") Long orderId);

    @PutMapping("/api/orders/{orderId}/payment-status")
    ApiResponse<Object> updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody PaymentStatusUpdateRequestDto requestDto
    );
}
