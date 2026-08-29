package com.foodorder.app.controller;

import com.foodorder.app.entity.Restaurant;
import com.foodorder.app.entity.User;
import com.foodorder.app.enums.Role;
import com.foodorder.app.service.RestaurantService;
import com.foodorder.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserService userService;

    // ---- Customer: browse restaurants ----
    @GetMapping
    public ResponseEntity<List<Restaurant>> browse() {
        return ResponseEntity.ok(restaurantService.browseActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    // ---- Restaurant Admin: create a restaurant they own ----
    @PostMapping
    public ResponseEntity<Restaurant> create(@RequestHeader("Authorization") String token,
                                              @Valid @RequestBody Restaurant restaurant) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(restaurantService.create(restaurant, owner));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Restaurant>> mine(@RequestHeader("Authorization") String token) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(restaurantService.ownedBy(owner.getId()));
    }

    // ---- Restaurant Admin: open/close for new orders ----
    @PatchMapping("/{id}/active")
    public ResponseEntity<Restaurant> setActive(@PathVariable Long id,
                                                 @RequestHeader("Authorization") String token,
                                                 @RequestParam boolean active) {
        User owner = userService.requireUser(token);
        userService.requireRole(owner, Role.RESTAURANT_ADMIN);
        return ResponseEntity.ok(restaurantService.setActive(id, owner, active));
    }
}
