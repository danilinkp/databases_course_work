package com.example.deliveryservice.dto.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateDeliveryZoneCommand(
        @NotBlank(message = "Название зоны доставки обязательно для заполнения")
        String zoneName,

        @NotBlank(message = "Почтовый индекс обязателен")
        @Size(min = 5, max = 10, message = "Некорректный формат почтового индекса")
        String postalCode,

        @NotNull(message = "Базовая стоимость доставки обязательна")
        @Min(value = 0, message = "Стоимость доставки не может быть отрицательной")
        BigDecimal deliveryFee,

        @NotNull(message = "Ближний порог расстояния обязателен")
        @Min(value = 0, message = "Порог расстояния не может быть отрицательным")
        BigDecimal nearThreshold,

        @NotNull(message = "Дальний порог расстояния обязателен")
        @Min(value = 0, message = "Порог расстояния не может быть отрицательным")
        BigDecimal farThreshold,

        @NotNull(message = "Стоимость за километр обязательна")
        @Min(value = 0, message = "Стоимость за км не может быть отрицательной")
        BigDecimal feePerKm,

        @NotNull(message = "Наценка в пиковые часы обязательна")
        @Min(value = 0, message = "Наценка не может быть отрицательной")
        BigDecimal peakSurcharge,

        @NotNull(message = "Наценка в выходные дни обязательна")
        @Min(value = 0, message = "Наценка не может быть отрицательной")
        BigDecimal weekendSurcharge,

        @NotNull(message = "Минимальная сумма заказа обязательна")
        @Min(value = 0, message = "Сумма заказа не может быть отрицательной")
        BigDecimal minOrderAmount,

        @NotNull(message = "Время доставки обязательно")
        @Min(value = 1, message = "Время доставки должно быть не менее 1 минуты")
        Integer deliveryTime
) {
}
