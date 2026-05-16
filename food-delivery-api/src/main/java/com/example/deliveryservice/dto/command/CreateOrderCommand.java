package com.example.deliveryservice.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        @NotNull(message = "ID ресторана обязательно")
        UUID restaurantId,

        @NotBlank(message = "Адрес доставки обязателен")
        String deliveryAddress,

        @NotNull(message = "Широта адреса доставки обязательна")
        BigDecimal deliveryLatitude,

        @NotNull(message = "Долгота адреса доставки обязательна")
        BigDecimal deliveryLongitude,

        @NotBlank(message = "Способ оплаты обязателен")
        String paymentMethod,

        @NotEmpty(message = "В заказе должно быть минимум одно блюдо")
        @Valid
        List<CreateOrderItemCommand> items
) {
}