package com.foodorder.app.dto;

import com.foodorder.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simplified session token. In a production system this would be a signed JWT;
 * here it's an opaque token mapped server-side to a user id (see TokenStore).
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private Role role;
}
