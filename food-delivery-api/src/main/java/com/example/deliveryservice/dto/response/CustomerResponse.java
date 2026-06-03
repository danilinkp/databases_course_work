package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Customer;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        Integer bonuses,
        Boolean isActive
) {
    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getBonuses(),
                customer.getIsActive()
        );
    }
}
