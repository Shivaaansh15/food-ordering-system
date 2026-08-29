package com.foodorder.app.service;

import com.foodorder.app.entity.CartItem;
import com.foodorder.app.entity.MenuItem;
import com.foodorder.app.entity.User;
import com.foodorder.app.exception.ApiException;
import com.foodorder.app.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backs the "Add to cart" AJAX calls from the menu page. Each call is a
 * small, fast, idempotent-ish operation so the frontend can update the
 * cart badge/total instantly without a full page reload.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final MenuItemService menuItemService;

    public List<CartItem> viewCart(Long customerId) {
        return cartItemRepository.findByCustomerId(customerId);
    }

    public CartItem addOrUpdateItem(User customer, Long menuItemId, int quantity) {
        if (quantity <= 0) {
            throw new ApiException("Quantity must be positive", HttpStatus.BAD_REQUEST);
        }
        MenuItem menuItem = menuItemService.getItem(menuItemId);
        if (!menuItem.isAvailable()) {
            throw new ApiException("This item is currently unavailable", HttpStatus.CONFLICT);
        }

        CartItem cartItem = cartItemRepository.findByCustomerIdAndMenuItemId(customer.getId(), menuItemId)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCustomer(customer);
                    ci.setMenuItem(menuItem);
                    return ci;
                });
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public void removeItem(Long customerId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApiException("Cart item not found", HttpStatus.NOT_FOUND));
        if (!item.getCustomer().getId().equals(customerId)) {
            throw new ApiException("Not your cart item", HttpStatus.FORBIDDEN);
        }
        cartItemRepository.delete(item);
    }

    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }
}
