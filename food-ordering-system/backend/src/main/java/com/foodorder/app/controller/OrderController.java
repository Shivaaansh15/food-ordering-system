package com.foodorder.app.controller;

import com.foodorder.app.dto.OrderStatusUpdateRequest;
import com.foodorder.app.dto.PlaceOrderRequest;
import com.foodorder.app.entity.Order;
import com.foodorder.app.entity.User;
import com.foodorder.app.enums.OrderStatus;
import com.foodorder.app.enums.Role;
import com.foodorder.app.exception.ApiException;
import com.foodorder.app.service.OrderService;
import com.foodorder.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    // ---- Customer: place order (cart -> order) ----
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestHeader("Authorization") String token,
                                             @Valid @RequestBody PlaceOrderRequest request) {
        User customer = userService.requireUser(token);
        userService.requireRole(customer, Role.CUSTOMER);
        Order order = orderService.placeOrder(customer, request.getRestaurantId(), request.getDeliveryAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // ---- Customer: order history ----
    @GetMapping("/history")
    public ResponseEntity<List<Order>> history(@RequestHeader("Authorization") String token) {
        User customer = userService.requireUser(token);
        return ResponseEntity.ok(orderService.historyForCustomer(customer.getId()));
    }

    // ---- Customer: track a single order's live status ----
    @GetMapping("/{orderId}/track")
    public ResponseEntity<Order> track(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        User customer = userService.requireUser(token);
        return ResponseEntity.ok(orderService.getOrderForCustomer(orderId, customer.getId()));
    }

    // ---- Customer: cancel a not-yet-accepted order ----
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancel(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        User customer = userService.requireUser(token);
        return ResponseEntity.ok(orderService.cancelOrder(orderId, customer));
    }

    // ---- Restaurant Admin: incoming orders for one of their restaurants ----
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Order>> forRestaurant(@RequestHeader("Authorization") String token,
                                                       @PathVariable Long restaurantId) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(orderService.ordersForRestaurant(restaurantId, owner));
    }

    // ---- Restaurant Admin: accept / reject / advance order status ----
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@RequestHeader("Authorization") String token,
                                               @PathVariable Long orderId,
                                               @Valid @RequestBody OrderStatusUpdateRequest request) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);

        OrderStatus target;
        try {
            target = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Unknown status: " + request.getStatus(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(orderService.updateStatus(orderId, owner, target));
    }
}
