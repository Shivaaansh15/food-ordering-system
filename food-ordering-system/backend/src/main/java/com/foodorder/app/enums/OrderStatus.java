package com.foodorder.app.enums;

/**
 * Lifecycle of an order.
 * PLACED -> ACCEPTED/REJECTED (by restaurant) -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
 * Any state before DELIVERED can move to CANCELLED.
 */
public enum OrderStatus {
    PLACED,
    ACCEPTED,
    REJECTED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
