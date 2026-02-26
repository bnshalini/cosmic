package com.cosmic.payment_service.service;

import com.cosmic.payment_service.client.OrderClient;
import com.cosmic.payment_service.dto.*;
import com.cosmic.payment_service.entity.Payment;
import com.cosmic.payment_service.enums.PaymentStatus;
import com.cosmic.payment_service.payload.ApiResponse;
import com.cosmic.payment_service.repository.PaymentRepository;
import com.cosmic.payment_service.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Override
    public RazorpayOrderResponseDto createRazorpayOrder(PaymentRequestDto dto) {

        // Validate order exists from order-service
        ApiResponse<OrderResponseDto> orderResponse = orderClient.getOrderById(dto.getOrderId());

        if (orderResponse == null || orderResponse.getData() == null) {
            throw new RuntimeException("Order not found with id: " + dto.getOrderId());
        }

        OrderResponseDto orderDto = orderResponse.getData();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", dto.getAmount().multiply(BigDecimal.valueOf(100))); // convert to paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + dto.getOrderId());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .orderId(dto.getOrderId())
                    .userId(dto.getUserId())
                    .amount(dto.getAmount())
                    .currency("INR")
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .paymentStatus(PaymentStatus.CREATED)
                    .build();

            payment = paymentRepository.save(payment);

            return RazorpayOrderResponseDto.builder()
                    .paymentId(payment.getId())
                    .orderId(dto.getOrderId())
                    .userId(dto.getUserId())
                    .amount(dto.getAmount())
                    .currency("INR")
                    .razorpayOrderId(payment.getRazorpayOrderId())
                    .razorpayKeyId(razorpayKeyId)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    @Override
    public String verifyPayment(PaymentVerifyRequestDto dto) {

        Payment payment = paymentRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Payment record not found for orderId: " + dto.getOrderId()));

        // 🔐 Idempotency protection
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return "Payment already verified";
        }

        try {

            String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            sha256Hmac.init(secretKey);

            byte[] hashBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hashBytes);

            if (!generatedSignature.equals(dto.getRazorpaySignature())) {

                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                orderClient.updatePaymentStatus(
                        dto.getOrderId(),
                        PaymentStatusUpdateRequestDto.builder()
                                .paymentStatus(PaymentStatus.FAILED)
                                .build()
                );

                throw new RuntimeException("Payment signature verification failed");
            }

            // ✅ Payment verified successfully
            payment.setRazorpayPaymentId(dto.getRazorpayPaymentId());
            payment.setRazorpaySignature(dto.getRazorpaySignature());
            payment.setPaymentStatus(PaymentStatus.PAID);

            paymentRepository.save(payment);

            orderClient.updatePaymentStatus(
                    dto.getOrderId(),
                    PaymentStatusUpdateRequestDto.builder()
                            .paymentStatus(PaymentStatus.PAID)
                            .build()
            );

            return "Payment verified successfully";

        } catch (Exception e) {

            if (payment.getPaymentStatus() != PaymentStatus.PAID) {

                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                orderClient.updatePaymentStatus(
                        dto.getOrderId(),
                        PaymentStatusUpdateRequestDto.builder()
                                .paymentStatus(PaymentStatus.FAILED)
                                .build()
                );
            }

            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }
}
