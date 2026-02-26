package com.cosmic.order_service.enums;

public enum OrderStatus {
    CREATED,        // order placed, payment pending
    CONFIRMED,      // payment success
    SHIPPED,
    DELIVERED,
    CANCELLED
}