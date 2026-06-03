package com.example.deliveryservice.services;


import com.example.deliveryservice.dto.command.CreateOrderCommand;
import com.example.deliveryservice.dto.command.CreateOrderItemCommand;
import com.example.deliveryservice.dto.command.OrderTotal;
import com.example.deliveryservice.entity.*;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.*;
import com.example.deliveryservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;
    private final CourierRepository courierRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final PricingService pricingService;

    @Transactional
    public Order create(UUID customerId, CreateOrderCommand command, CustomUserDetails currentUser) {
        // Check if the current user is creating an order for themselves or is an admin
        if (!isOwnCustomer(customerId, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + command.restaurantId()));

        if (!restaurant.getIsActive()) {
            throw new IllegalStateException("Restaurant is not active");
        }

        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .orderNumber(generateOrderNumber())
                .customerName(customer.getFullName())
                .customerPhone(customer.getPhone())
                .restaurantName(restaurant.getName())
                .deliveryAddress(command.deliveryAddress())
                .deliveryLatitude(command.deliveryLatitude())
                .deliveryLongitude(command.deliveryLongitude())
                .status(OrderStatus.pending)
                .subtotal(BigDecimal.ZERO)
                .deliveryFee(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderItemCommand itemCmd : command.items()) {
            Dish dish = dishRepository.findById(itemCmd.dishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + itemCmd.dishId()));

            if (!dish.getIsAvailable()) {
                throw new IllegalStateException("Dish is not available: " + dish.getName());
            }

            items.add(OrderItem.builder()
                    .order(savedOrder)
                    .dish(dish)
                    .unitPrice(dish.getPrice())
                    .quantity(itemCmd.quantity())
                    .specialRequests(itemCmd.specialRequests())
                    .build());
        }
        orderItemRepository.saveAll(items);

        OrderTotal total = pricingService.calculate(savedOrder.getId());
        savedOrder.setSubtotal(total.subtotal());
        savedOrder.setDeliveryFee(total.deliveryFee());
        savedOrder.setDiscount(total.discount());
        savedOrder.setTotalAmount(total.totalAmount());

        return orderRepository.save(savedOrder);
    }

    @Transactional(readOnly = true)
    public Order getById(UUID id, CustomUserDetails currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // Check if the current user is the customer of this order, or a courier assigned to this order, or restaurant owner, or admin
        if (!isOrderAccessible(id, currentUser)) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }

        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getByCustomerId(UUID customerId, CustomUserDetails currentUser) {
        // Check if the current user is requesting their own orders or is an admin
        if (!isOwnCustomer(customerId, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getByRestaurantId(UUID restaurantId, CustomUserDetails currentUser) {
        // Check if the current user owns this restaurant or is an admin
        if (!isOwnRestaurant(restaurantId, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }

        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public Order confirm(UUID orderId, CustomUserDetails currentUser) {
        Order order = getById(orderId, currentUser);

        if (order.getStatus() != OrderStatus.pending) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }

        order.setStatus(OrderStatus.confirmed);
        order.setConfirmedAt(Instant.from(LocalDateTime.now()));
        return orderRepository.save(order);
    }

    public Order startPreparing(UUID orderId, CustomUserDetails currentUser) {
        Order order = getById(orderId, currentUser);

        if (order.getStatus() != OrderStatus.confirmed) {
            throw new IllegalStateException("Only confirmed orders can start preparing");
        }

        order.setStatus(OrderStatus.preparing);
        return orderRepository.save(order);
    }

    public Order markReady(UUID orderId, CustomUserDetails currentUser) {
        Order order = getById(orderId, currentUser);

        if (order.getStatus() != OrderStatus.preparing) {
            throw new IllegalStateException("Order is not being prepared");
        }

        order.setStatus(OrderStatus.ready);
        return orderRepository.save(order);
    }

    public Order assignCourier(UUID orderId, UUID courierId, CustomUserDetails currentUser) {
        Order order = getById(orderId, currentUser);

        if (order.getStatus() != OrderStatus.ready) {
            throw new IllegalStateException("Courier can only be assigned to ready orders");
        }

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found: " + courierId));

        if (!courier.getIsAvailable() || !courier.getIsActive()) {
            throw new IllegalStateException("Courier is not available");
        }

        order.setCourier(courier);
        order.setStatus(OrderStatus.delivering);

        courier.setIsAvailable(false);
        courierRepository.save(courier);

        return orderRepository.save(order);
    }

    public Order completeDelivery(UUID orderId, CustomUserDetails currentUser) {
        Order order = getById(orderId, currentUser);

        if (order.getStatus() != OrderStatus.delivering) {
            throw new IllegalStateException("Order is not in delivering state");
        }

        order.setStatus(OrderStatus.delivered);
        order.setDeliveredAt(Instant.from(LocalDateTime.now()));

        Courier courier = order.getCourier();
        if (courier != null) {
            courier.setIsAvailable(true);
            courierRepository.save(courier);
        }

        return orderRepository.save(order);
    }

    public Order cancel(UUID orderId, CustomUserDetails currentUser) {
        Order order = getById(orderId, currentUser);

        if (order.getStatus() == OrderStatus.delivering
                || order.getStatus() == OrderStatus.delivered) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.cancelled);
        order.setCancelledAt(Instant.from(LocalDateTime.now()));

        Courier courier = order.getCourier();
        if (courier != null) {
            courier.setIsAvailable(true);
            courierRepository.save(courier);
        }

        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    /**
     * Check if the requested customer ID matches the current user's ID.
     */
    private boolean isOwnCustomer(UUID customerId, CustomUserDetails currentUser) {
        try {
            UUID userId = UUID.fromString(currentUser.getId());
            return userId.equals(customerId) && "customer_role".equals(currentUser.getRole());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if the requested restaurant ID matches the current user's restaurant.
     * This assumes that restaurant admins have a way to know which restaurant they own.
     * For simplicity, we'll check if the user has restaurant_admin_role and then
     * we might need to check which restaurant they own. But since we don't have
     * a direct link from user to restaurant in the auth system, we'll rely on
     * the fact that restaurant admins should only access their own restaurant
     * through other means (like having restaurant_id in their token or profile).
     * For now, we'll allow any restaurant_admin to access any restaurant (not ideal but simple).
     * A better approach would be to add restaurant_id to the user details when a restaurant admin logs in.
     */
    private boolean isOwnRestaurant(UUID restaurantId, CustomUserDetails currentUser) {
        // For now, we'll just check if the user is a restaurant admin and allow access
        // In a production system, you'd want to check which specific restaurant they own
        return "restaurant_admin_role".equals(currentUser.getRole());
    }

    /**
     * Check if the current user has access to an order.
     * Access is granted if:
     * 1. The user is the customer who placed the order
     * 2. The user is the courier assigned to the order
     * 3. The user is the restaurant owner (restaurant_admin_role)
     * 4. The user is a system admin
     */
    private boolean isOrderAccessible(UUID orderId, CustomUserDetails currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        try {
            UUID userId = UUID.fromString(currentUser.getId());
            String userRole = currentUser.getRole();

            // Check if user is the customer
            if ("customer_role".equals(userRole) && order.getCustomer().getId().equals(userId)) {
                return true;
            }

            // Check if user is the assigned courier
            if ("courier_role".equals(userRole) && order.getCourier() != null && order.getCourier().getId().equals(userId)) {
                return true;
            }

            // Check if user is the restaurant owner (simplified)
            if ("restaurant_admin_role".equals(userRole) && order.getRestaurant().getId().equals(userId)) {
                // This assumes the userId in the token is actually the restaurant ID for restaurant admins
                // This is not ideal - we'd need a better way to link restaurant admins to their restaurants
                return true;
            }

            // Check if user is system admin
            if ("system_admin_role".equals(userRole)) {
                return true;
            }

            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if the current user has admin role.
     */
    private boolean isAdmin(CustomUserDetails currentUser) {
        return "system_admin_role".equals(currentUser.getRole());
    }
}