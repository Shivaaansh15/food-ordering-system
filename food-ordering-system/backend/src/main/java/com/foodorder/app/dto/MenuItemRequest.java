package com.foodorder.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemRequest {
    @NotBlank
    private String name;
    private String description;
    private String category;
    @NotNull
    private BigDecimal price;
    private String imageUrl;
}
