package com.foodorder.app.repository;

import com.foodorder.app.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId);
    List<Order> findByRestaurantIdOrderByPlacedAtDesc(Long restaurantId);
}
