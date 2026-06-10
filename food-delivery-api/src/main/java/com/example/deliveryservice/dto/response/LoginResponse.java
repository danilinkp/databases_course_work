package com.example.deliveryservice.dto.response;

public record LoginResponse(
        String token,
        String role,
        String id
) {
}