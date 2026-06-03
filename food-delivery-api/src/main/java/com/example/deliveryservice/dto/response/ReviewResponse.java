package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Review;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID orderId,
        Integer restaurantRating,
        Integer courierRating,
        Integer deliverySpeed,
        String comment,
        Instant createdAt
) {
    public static ReviewResponse fromEntity(Review review) {
        if (review == null) return null;
        return new ReviewResponse(
                review.getId(),
                review.getOrder().getId(),
                review.getRestaurantRating(),
                review.getCourierRating(),
                review.getDeliverySpeed(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}