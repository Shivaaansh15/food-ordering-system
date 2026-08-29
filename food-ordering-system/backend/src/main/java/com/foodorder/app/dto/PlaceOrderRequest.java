package com.foodorder.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull
    private Long restaurantId;
    @NotBlank
    private String deliveryAddress;
}
