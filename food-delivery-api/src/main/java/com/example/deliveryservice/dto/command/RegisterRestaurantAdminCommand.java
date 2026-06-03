package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RegisterRestaurantAdminCommand(
        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        String password,

        @NotBlank(message = "Token приглашения обязателен")
        String invitationToken,

        UUID restaurantId
) {
}