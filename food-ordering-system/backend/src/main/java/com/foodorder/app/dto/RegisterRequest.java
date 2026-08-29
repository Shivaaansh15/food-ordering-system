package com.foodorder.app.dto;

import com.foodorder.app.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    private String phone;
    private String address;

    @NotBlank
    private String role; // "CUSTOMER" or "RESTAURANT_ADMIN"
}
