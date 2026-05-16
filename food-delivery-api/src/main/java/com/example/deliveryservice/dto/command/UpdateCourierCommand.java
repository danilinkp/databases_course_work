package com.example.deliveryservice.dto.command;

import com.example.deliveryservice.entity.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCourierCommand(
        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
        String fullName,

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Телефон не должен быть пустым")
        String phone,

        @NotBlank(message = "Место работы не может быть пустым")
        String areaOfWork,

        @NotNull(message = "Тип транспорта обязателен для заполнения")
        VehicleType vehicleType
) {
}
