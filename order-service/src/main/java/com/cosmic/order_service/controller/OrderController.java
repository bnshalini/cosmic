package com.cosmic.order_service.controller;

import com.cosmic.order_service.dto.OrderResponseDto;
import com.cosmic.order_service.dto.PaymentStatusUpdateRequestDto;
import com.cosmic.order_service.enums.OrderStatus;
import com.cosmic.order_service.payload.ApiResponse;
import com.cosmic.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place/{userId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(@PathVariable Long userId) {

        OrderResponseDto order = orderService.placeOrder(userId);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponseDto>builder()
                        .success(true)
                        .message("Order placed successfully")
                        .data(order)
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long orderId) {

        OrderResponseDto order = orderService.getOrderById(orderId);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponseDto>builder()
                        .success(true)
                        .message("Order fetched successfully")
                        .data(order)
                        .build()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByUserId(@PathVariable Long userId) {

        List<OrderResponseDto> orders = orderService.getOrdersByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponseDto>>builder()
                        .success(true)
                        .message("Orders fetched successfully")
                        .data(orders)
                        .build()
        );
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(@PathVariable Long orderId) {

        OrderResponseDto order = orderService.cancelOrder(orderId);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponseDto>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .data(order)
                        .build()
        );
    }

    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestBody PaymentStatusUpdateRequestDto requestDto
    ) {

        OrderResponseDto response = orderService.updatePaymentStatus(orderId, requestDto);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponseDto>builder()
                        .success(true)
                        .message("Payment status updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, newStatus));
    }
}
