package com.foodorder.app.repository;

import com.foodorder.app.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerId(Long customerId);
    Optional<CartItem> findByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);
    void deleteByCustomerId(Long customerId);
}
