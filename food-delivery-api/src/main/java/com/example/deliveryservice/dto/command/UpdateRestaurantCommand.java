package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record UpdateRestaurantCommand(
        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
        String name,

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Телефон не должен быть пустым")
        String phone,

        @NotBlank(message = "Тип кухни не должен быть пустым")
        String cuisineType,

        @NotBlank(message = "Адрес не должен быть пустым")
        String address,

        @NotNull(message = "Широта обязательна")
        Double latitude,

        @NotNull(message = "Долгота обязательна")
        Double longitude,

        @NotBlank(message = "Время открытия не может быть пустым")
        LocalTime openingTime,

        @NotBlank(message = "Время закрытия не может быть пустым")
        LocalTime closingTime
) {
}
