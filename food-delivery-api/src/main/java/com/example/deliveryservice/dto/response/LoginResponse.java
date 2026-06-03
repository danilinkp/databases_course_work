package com.example.deliveryservice.dto.response;

/**
 * Login response DTO.
 */
public record LoginResponse(
        String token,
        String role,
        String id
) {
}