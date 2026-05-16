package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReviewCommand(
        @NotNull(message = "ID заказа обязателен")
        UUID orderId,

        @Min(value = 1, message = "Минимальная оценка ресторану — 1")
        @Max(value = 5, message = "Максимальная оценка ресторану — 5")
        Integer restaurantRating,

        @Min(value = 1, message = "Минимальная оценка курьеру — 1")
        @Max(value = 5, message = "Максимальная оценка курьеру — 5")
        Integer courierRating,

        @Min(value = 1, message = "Минимальная оценка скорости доставки — 1")
        @Max(value = 5, message = "Максимальная оценка скорости доставки — 5")
        Integer deliverySpeed,

        @Size(max = 1000, message = "Комментарий не может быть длиннее 1000 символов")
        String comment
) {
}
