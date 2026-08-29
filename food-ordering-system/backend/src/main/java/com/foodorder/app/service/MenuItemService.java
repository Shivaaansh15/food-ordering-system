package com.foodorder.app.service;

import com.foodorder.app.entity.MenuItem;
import com.foodorder.app.entity.Restaurant;
import com.foodorder.app.entity.User;
import com.foodorder.app.exception.ApiException;
import com.foodorder.app.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    public List<MenuItem> availableMenuFor(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    public List<MenuItem> fullMenuFor(Long restaurantId, User requestingOwner) {
        Restaurant restaurant = restaurantService.getById(restaurantId);
        restaurantService.assertOwnership(restaurant, requestingOwner);
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public MenuItem addItem(Long restaurantId, User owner, MenuItem item) {
        Restaurant restaurant = restaurantService.getById(restaurantId);
        restaurantService.assertOwnership(restaurant, owner);
        item.setRestaurant(restaurant);
        item.setAvailable(true);
        return menuItemRepository.save(item);
    }

    public MenuItem updatePrice(Long itemId, User owner, BigDecimal newPrice) {
        MenuItem item = getOwnedItem(itemId, owner);
        item.setPrice(newPrice);
        return menuItemRepository.save(item);
    }

    public MenuItem setAvailability(Long itemId, User owner, boolean available) {
        MenuItem item = getOwnedItem(itemId, owner);
        item.setAvailable(available);
        return menuItemRepository.save(item);
    }

    public MenuItem getItem(Long itemId) {
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("Menu item not found", HttpStatus.NOT_FOUND));
    }

    private MenuItem getOwnedItem(Long itemId, User owner) {
        MenuItem item = getItem(itemId);
        restaurantService.assertOwnership(item.getRestaurant(), owner);
        return item;
    }
}
