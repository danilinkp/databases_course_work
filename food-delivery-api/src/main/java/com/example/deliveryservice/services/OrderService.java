package com.example.deliveryservice.services;


import com.example.deliveryservice.dto.command.CreateOrderCommand;
import com.example.deliveryservice.dto.command.CreateOrderItemCommand;
import com.example.deliveryservice.dto.command.OrderTotal;
import com.example.deliveryservice.entity.*;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.*;
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

    public Order create(UUID customerId, CreateOrderCommand command) {
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
                    .dishName(dish.getName())
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
    public Order getById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        return orderRepository.findByCustomerCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getByRestaurantId(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public Order confirm(UUID orderId) {
        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.pending) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }

        order.setStatus(OrderStatus.confirmed);
        order.setConfirmedAt(Instant.from(LocalDateTime.now()));
        return orderRepository.save(order);
    }

    public Order startPreparing(UUID orderId) {
        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.confirmed) {
            throw new IllegalStateException("Only confirmed orders can start preparing");
        }

        order.setStatus(OrderStatus.preparing);
        return orderRepository.save(order);
    }

    public Order markReady(UUID orderId) {
        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.preparing) {
            throw new IllegalStateException("Order is not being prepared");
        }

        order.setStatus(OrderStatus.ready);
        return orderRepository.save(order);
    }

    public Order assignCourier(UUID orderId, UUID courierId) {
        Order order = getById(orderId);

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

    public Order completeDelivery(UUID orderId) {
        Order order = getById(orderId);

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

    public Order cancel(UUID orderId) {
        Order order = getById(orderId);

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
}
