package com.foodorder.app.controller;

import com.foodorder.app.dto.CartItemRequest;
import com.foodorder.app.entity.CartItem;
import com.foodorder.app.entity.User;
import com.foodorder.app.service.CartService;
import com.foodorder.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Every endpoint here is designed to be called via AJAX (fetch) from the
 * menu page so the cart badge/subtotal update instantly, without a full
 * page reload. See frontend/js/app.js -> addToCart().
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<CartItem>> viewCart(@RequestHeader("Authorization") String token) {
        User customer = userService.requireUser(token);
        return ResponseEntity.ok(cartService.viewCart(customer.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItem> addOrUpdateItem(@RequestHeader("Authorization") String token,
                                                      @Valid @RequestBody CartItemRequest request) {
        User customer = userService.requireUser(token);
        return ResponseEntity.ok(cartService.addOrUpdateItem(customer, request.getMenuItemId(), request.getQuantity()));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeItem(@RequestHeader("Authorization") String token,
                                                            @PathVariable Long cartItemId) {
        User customer = userService.requireUser(token);
        cartService.removeItem(customer.getId(), cartItemId);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(@RequestHeader("Authorization") String token) {
        User customer = userService.requireUser(token);
        cartService.clearCart(customer.getId());
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }
}
