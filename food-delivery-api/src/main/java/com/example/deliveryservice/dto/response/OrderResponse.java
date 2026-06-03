package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        String customerName,
        String customerPhone,
        UUID restaurantId,
        String restaurantName,
        UUID courierId,
        String deliveryAddress,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal discount,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        Instant confirmedAt,
        Instant deliveredAt,
        Instant cancelledAt
) {
    public static OrderResponse fromEntity(Order order, List<OrderItemResponse> items) {
        if (order == null) return null;
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getRestaurant().getId(),
                order.getRestaurantName(),
                order.getCourier() != null ? order.getCourier().getId() : null,
                order.getDeliveryAddress(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getDiscount(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt()
        );
    }
}