package com.foodorder.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotBlank
    private String status; // ACCEPTED, REJECTED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}
