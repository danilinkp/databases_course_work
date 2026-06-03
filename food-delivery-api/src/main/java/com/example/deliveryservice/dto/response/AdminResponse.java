package com.example.deliveryservice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminResponse(
        UUID id,
        String email,
        String role,
        List<UUID> restaurantIds,
        boolean isActive,
        Instant createdAt
) {
}