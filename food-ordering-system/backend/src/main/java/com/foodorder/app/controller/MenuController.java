package com.foodorder.app.controller;

import com.foodorder.app.dto.AvailabilityUpdateRequest;
import com.foodorder.app.dto.MenuItemRequest;
import com.foodorder.app.dto.PriceUpdateRequest;
import com.foodorder.app.entity.MenuItem;
import com.foodorder.app.entity.User;
import com.foodorder.app.enums.Role;
import com.foodorder.app.service.MenuItemService;
import com.foodorder.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuItemService menuItemService;
    private final UserService userService;

    // ---- Customer: view a restaurant's available menu ----
    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<MenuItem>> viewMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemService.availableMenuFor(restaurantId));
    }

    // ---- Restaurant Admin: full menu incl. unavailable items ----
    @GetMapping("/restaurants/{restaurantId}/menu/manage")
    public ResponseEntity<List<MenuItem>> manageMenu(@PathVariable Long restaurantId,
                                                       @RequestHeader("Authorization") String token) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(menuItemService.fullMenuFor(restaurantId, owner));
    }

    // ---- Restaurant Admin: add a food item ----
    @PostMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<MenuItem> addItem(@PathVariable Long restaurantId,
                                             @RequestHeader("Authorization") String token,
                                             @Valid @RequestBody MenuItemRequest request) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);

        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl());

        return ResponseEntity.ok(menuItemService.addItem(restaurantId, owner, item));
    }

    // ---- Restaurant Admin: update price ----
    @PatchMapping("/menu/{itemId}/price")
    public ResponseEntity<MenuItem> updatePrice(@PathVariable Long itemId,
                                                 @RequestHeader("Authorization") String token,
                                                 @Valid @RequestBody PriceUpdateRequest request) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(menuItemService.updatePrice(itemId, owner, request.getPrice()));
    }

    // ---- Restaurant Admin: toggle availability (e.g. "sold out") ----
    @PatchMapping("/menu/{itemId}/availability")
    public ResponseEntity<MenuItem> setAvailability(@PathVariable Long itemId,
                                                      @RequestHeader("Authorization") String token,
                                                      @RequestBody AvailabilityUpdateRequest request) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(menuItemService.setAvailability(itemId, owner, request.isAvailable()));
    }
}
