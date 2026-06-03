package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.DishCategory;
import java.util.UUID;

public record DishCategoryResponse(
        UUID id,
        String name,
        String description,
        Integer displayOrder
) {
    public static DishCategoryResponse fromEntity(DishCategory category) {
        if (category == null) return null;
        return new DishCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder()
        );
    }
}