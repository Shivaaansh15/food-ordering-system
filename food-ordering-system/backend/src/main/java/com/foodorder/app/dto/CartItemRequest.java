package com.foodorder.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    private Long menuItemId;
    @Min(1)
    private int quantity;
}
