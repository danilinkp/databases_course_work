package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AdminRegisterCommand(
        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        String password,

        String role,

        List<String> restaurantIds
) {
}