package com.foodorder.app.service;

import com.foodorder.app.entity.Restaurant;
import com.foodorder.app.entity.User;
import com.foodorder.app.exception.ApiException;
import com.foodorder.app.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> browseActive() {
        return restaurantRepository.findByActiveTrue();
    }

    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ApiException("Restaurant not found", HttpStatus.NOT_FOUND));
    }

    public Restaurant create(Restaurant restaurant, User owner) {
        restaurant.setOwner(owner);
        restaurant.setActive(true);
        return restaurantRepository.save(restaurant);
    }

    public List<Restaurant> ownedBy(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    public Restaurant setActive(Long restaurantId, User owner, boolean active) {
        Restaurant r = getById(restaurantId);
        assertOwnership(r, owner);
        r.setActive(active);
        return restaurantRepository.save(r);
    }

    public void assertOwnership(Restaurant restaurant, User owner) {
        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new ApiException("You do not manage this restaurant", HttpStatus.FORBIDDEN);
        }
    }
}
