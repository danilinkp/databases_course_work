package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID dishId,
        String dishName,
        BigDecimal unitPrice,
        Integer quantity,
        String specialRequests
) {
    public static OrderItemResponse fromEntity(OrderItem item) {
        if (item == null) return null;
        return new OrderItemResponse(
                item.getId(),
                item.getDish().getId(),
                item.getDish().getName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSpecialRequests()
        );
    }
}