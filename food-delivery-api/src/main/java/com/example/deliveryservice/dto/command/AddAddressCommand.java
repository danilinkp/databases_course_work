package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record AddAddressCommand(
        @NotBlank(message = "Регион обязателен для заполнения")
        String region,

        @NotBlank(message = "Город обязателен для заполнения")
        @Size(max = 100, message = "Название города слишком длинное")
        String city,

        @NotBlank(message = "Улица обязательна для заполнения")
        String street,

        @NotBlank(message = "Номер дома обязателен")
        String house,

        String apartment,
        String addressDetails,

        @NotBlank(message = "Почтовый индекс обязателен")
        @Size(min = 5, max = 10, message = "Некорректный формат индекса")
        String postalCode,

        @NotNull(message = "Широта обязательна")
        Double latitude,

        @NotNull(message = "Долгота обязательна")
        Double longitude
) {
}
