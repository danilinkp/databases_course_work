package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrderItemCommand(
        @NotNull(message = "ID блюда обязательно")
        UUID dishId,

        @NotNull(message = "Количество товара обязательно")
        @Min(value = 1, message = "Количество товара должно быть не менее 1")
        Integer quantity,

        @Size(max = 500, message = "Комментарий к блюду слишком длинный")
        String specialRequests
) {
}