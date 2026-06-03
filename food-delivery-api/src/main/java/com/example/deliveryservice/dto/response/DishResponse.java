package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Dish;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record DishResponse(
        UUID id,
        UUID restaurantId,
        List<DishCategoryResponse> categories,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Boolean isAvailable,
        Boolean isSpicy,
        Integer preparationTime,
        Integer calories,
        Integer weightGrams
) {
    public static DishResponse fromEntity(Dish dish) {
        if (dish == null) return null;
        return new DishResponse(
                dish.getId(),
                dish.getRestaurant().getId(),
                dish.getCategories().stream()
                        .map(DishCategoryResponse::fromEntity)
                        .collect(Collectors.toList()),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getImageUrl(),
                dish.getIsAvailable(),
                dish.getIsSpicy(),
                dish.getPreparationTime(),
                dish.getCalories(),
                dish.getWeightGrams()
        );
    }
}