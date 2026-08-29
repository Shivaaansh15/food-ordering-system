package com.foodorder.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceUpdateRequest {
    @NotNull
    private BigDecimal price;
}
