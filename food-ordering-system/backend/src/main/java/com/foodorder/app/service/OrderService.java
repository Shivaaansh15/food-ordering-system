package com.foodorder.app.service;

import com.foodorder.app.entity.*;
import com.foodorder.app.enums.OrderStatus;
import com.foodorder.app.exception.ApiException;
import com.foodorder.app.repository.CartItemRepository;
import com.foodorder.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final RestaurantService restaurantService;

    // Valid forward transitions a restaurant/customer may make from a given state.
    private static final Set<OrderStatus> RESTAURANT_ONLY_TRANSITIONS =
            EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.REJECTED, OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED);

    /**
     * Places an order from the customer's current cart.
     *
     * Wrapped in @Transactional so the "read cart -> build order -> clear cart"
     * sequence is atomic: if anything fails, nothing is partially committed -
     * this is important once many customers are checking out concurrently.
     */
    @Transactional
    public Order placeOrder(User customer, Long restaurantId, String deliveryAddress) {
        Restaurant restaurant = restaurantService.getById(restaurantId);
        if (!restaurant.isActive()) {
            throw new ApiException("This restaurant is not accepting orders right now", HttpStatus.CONFLICT);
        }

        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customer.getId());
        if (cartItems.isEmpty()) {
            throw new ApiException("Cart is empty", HttpStatus.BAD_REQUEST);
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus(OrderStatus.PLACED);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            if (!ci.getMenuItem().getRestaurant().getId().equals(restaurantId)) {
                throw new ApiException("Cart contains items from a different restaurant", HttpStatus.BAD_REQUEST);
            }
            if (!ci.getMenuItem().isAvailable()) {
                throw new ApiException(ci.getMenuItem().getName() + " is no longer available", HttpStatus.CONFLICT);
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setMenuItem(ci.getMenuItem());
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtOrderTime(ci.getMenuItem().getPrice());
            order.getItems().add(oi);

            total = total.add(ci.getMenuItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // Clear the cart now that it has become an order
        cartItemRepository.deleteByCustomerId(customer.getId());

        // Notify the restaurant off the request thread (multithreading via
        // Spring's @Async + the "orderTaskExecutor" pool from AsyncConfig).
        notifyRestaurantOfNewOrder(saved);

        return saved;
    }

    /**
     * Simulates asynchronous work (e.g. push notification, kitchen display
     * update, SMS to the restaurant) that shouldn't block the HTTP response
     * the customer is waiting on. Runs on the bounded "orderTaskExecutor"
     * thread pool, so many simultaneous checkouts are processed concurrently
     * without exhausting the web server's own request-handling threads.
     */
    @Async("orderTaskExecutor")
    public void notifyRestaurantOfNewOrder(Order order) {
        log.info("[{}] New order #{} received for restaurant '{}' - total {}",
                Thread.currentThread().getName(), order.getId(), order.getRestaurant().getName(), order.getTotalAmount());
    }

    public List<Order> historyForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId);
    }

    public List<Order> ordersForRestaurant(Long restaurantId, User owner) {
        Restaurant restaurant = restaurantService.getById(restaurantId);
        restaurantService.assertOwnership(restaurant, owner);
        return orderRepository.findByRestaurantIdOrderByPlacedAtDesc(restaurantId);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
    }

    public Order getOrderForCustomer(Long orderId, Long customerId) {
        Order order = getOrder(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ApiException("Not your order", HttpStatus.FORBIDDEN);
        }
        return order;
    }

    /**
     * Restaurant admin accepts, rejects, or advances an order's status.
     * synchronized per-order-id would be needed for extreme concurrency on a
     * single order; here @Transactional + the DB row lock on save is enough
     * since a single order is only ever updated by its one owning restaurant.
     */
    @Transactional
    public Order updateStatus(Long orderId, User owner, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        restaurantService.assertOwnership(order.getRestaurant(), owner);

        if (!RESTAURANT_ONLY_TRANSITIONS.contains(newStatus)) {
            throw new ApiException("Invalid target status: " + newStatus, HttpStatus.BAD_REQUEST);
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REJECTED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ApiException("Order is already finalized (" + order.getStatus() + ")", HttpStatus.CONFLICT);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId, User customer) {
        Order order = getOrderForCustomer(orderId, customer.getId());
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new ApiException("Order can no longer be cancelled", HttpStatus.CONFLICT);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}
