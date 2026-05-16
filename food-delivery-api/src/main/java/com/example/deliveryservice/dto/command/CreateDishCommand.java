package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDishCommand(
        @NotNull(message = "ID ресторана обязательно")
        UUID restaurantId,

        @NotNull(message = "ID категории обязательно")
        UUID categoryId,

        @NotBlank(message = "Название блюда обязательно для заполнения")
        @Size(max = 150, message = "Название блюда не может превышать 150 символов")
        String name,

        String description,

        @NotNull(message = "Цена обязательна для заполнения")
        @DecimalMin(value = "0.0", inclusive = false, message = "Цена блюда должна быть больше нуля")
        BigDecimal price,

        String imageUrl,

        Boolean isAvailable,
        Boolean isSpicy,

        @Min(value = 1, message = "Время приготовления должно быть не менее 1 минуты")
        Integer preparationTime,

        @Min(value = 0, message = "Количество калорий не может быть отрицательным")
        Integer calories,

        @Min(value = 1, message = "Вес блюда должен быть больше 0")
        Integer weightGrams
) {
}
