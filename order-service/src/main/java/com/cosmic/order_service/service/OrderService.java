package com.cosmic.order_service.service;

import com.cosmic.order_service.dto.OrderResponseDto;
import com.cosmic.order_service.dto.PaymentStatusUpdateRequestDto;
import com.cosmic.order_service.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDto placeOrder(Long userId);

    OrderResponseDto getOrderById(Long orderId);

    List<OrderResponseDto> getOrdersByUserId(Long userId);

    OrderResponseDto cancelOrder(Long orderId);

    OrderResponseDto updatePaymentStatus(Long orderId, PaymentStatusUpdateRequestDto requestDto);

    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus);
}
