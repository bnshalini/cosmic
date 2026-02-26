package com.cosmic.order_service.service;

import com.cosmic.order_service.client.CartClient;
import com.cosmic.order_service.client.ProductClient;
import com.cosmic.order_service.dto.*;
import com.cosmic.order_service.entity.Order;
import com.cosmic.order_service.entity.OrderItem;
import com.cosmic.order_service.enums.OrderStatus;
import com.cosmic.order_service.enums.PaymentStatus;
import com.cosmic.order_service.exception.ResourceNotFoundException;
import com.cosmic.order_service.payload.ApiResponse;
import com.cosmic.order_service.repository.OrderItemRepository;
import com.cosmic.order_service.repository.OrderRepository;
import com.cosmic.order_service.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderResponseDto placeOrder(Long userId) {

        ApiResponse<CartResponseDto> cartResponse = cartClient.getCartByUserId(userId);

        if (cartResponse == null || cartResponse.getData() == null || cartResponse.getData().getItems().isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty for userId: " + userId);
        }

        CartResponseDto cart = cartResponse.getData();

        Order order = Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(cart.getGrandTotal())
                .build();

        List<CartItemResponseDto> cartItems = cart.getItems();

        try {

            // Step 1: Reserve stock for all items
            for (CartItemResponseDto cartItem : cartItems) {
                productClient.reserveStock(cartItem.getProductId(), cartItem.getQuantity());
            }

            // Step 2: Save Order
            order = orderRepository.save(order);

            // Step 3: Save Order Items
            for (CartItemResponseDto cartItem : cartItems) {

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProductId())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getPrice())
                        .build();

                orderItemRepository.save(orderItem);
            }

            // Step 4: Clear Cart
            cartClient.clearCart(userId);

            return getOrderById(order.getId());

        } catch (Exception e) {

            // 🔥 Compensation Logic
            // If anything fails after reservation, release reserved stock

            for (CartItemResponseDto cartItem : cartItems) {
                try {
                    productClient.releaseStock(cartItem.getProductId(), cartItem.getQuantity());
                } catch (Exception ex) {
                    // Log but do not suppress original exception
                    System.err.println("Failed to release stock for productId: "
                            + cartItem.getProductId());
                }
            }

            throw new RuntimeException("Order placement failed. All reserved stock released.", e);
        }
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        List<OrderItemResponseDto> responseItems = orderItems.stream()
                .map(item -> {

                    ApiResponse<ProductResponseDto> productResponse =
                            productClient.getProductById(item.getProductId());

                    ProductResponseDto product =
                            productResponse != null ? productResponse.getData() : null;

                    return OrderItemResponseDto.builder()
                            .productId(item.getProductId())
                            .productName(product != null ? product.getName() : null)
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .totalPrice(item.getTotalPrice())
                            .build();
                })
                .toList();

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .items(responseItems)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    public List<OrderResponseDto> getOrdersByUserId(Long userId) {

        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(order -> getOrderById(order.getId()))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));

        // Only allow cancel if still not confirmed
        if (order.getOrderStatus() != OrderStatus.CREATED ||
                order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled at this stage");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            productClient.releaseStock(item.getProductId(), item.getQuantity());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return getOrderById(orderId);
    }

    @Override
    @Transactional
    public OrderResponseDto updatePaymentStatus(
            Long orderId,
            PaymentStatusUpdateRequestDto requestDto) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment already processed for this order");
        }

        PaymentStatus newStatus = requestDto.getPaymentStatus();

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (newStatus == PaymentStatus.PAID) {

            // 🔹 Confirm stock permanently
            for (OrderItem item : orderItems) {
                productClient.confirmStock(item.getProductId(), item.getQuantity());
            }

            order.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }

        else if (newStatus == PaymentStatus.FAILED) {

            // 🔹 Release reserved stock
            for (OrderItem item : orderItems) {
                productClient.releaseStock(item.getProductId(), item.getQuantity());
            }

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
        }

        else {
            throw new RuntimeException("Invalid payment status update");
        }

        orderRepository.save(order);

        return getOrderById(orderId);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED ||
                order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Order status cannot be changed");
        }

        if (order.getOrderStatus() == OrderStatus.CONFIRMED && newStatus != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order must be SHIPPED after CONFIRMED");
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED && newStatus != OrderStatus.DELIVERED) {
            throw new RuntimeException("Order must be DELIVERED after SHIPPED");
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        return getOrderById(orderId);
    }
}
