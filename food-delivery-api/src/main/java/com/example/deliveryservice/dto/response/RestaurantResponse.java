package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Restaurant;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String cuisineType,
        BigDecimal rating,
        String address,
        String phone,
        String email,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean isActive
) {
    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        if (restaurant == null) return null;
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCuisineType(),
                restaurant.getRating(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getOpeningTime(),
                restaurant.getClosingTime(),
                restaurant.getIsActive()
        );
    }
}