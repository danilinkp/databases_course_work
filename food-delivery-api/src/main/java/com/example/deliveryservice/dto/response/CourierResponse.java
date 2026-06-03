package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Courier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CourierResponse(
        UUID id,
        String fullName,
        String phone,
        String email,
        LocalDate employeeDate,
        String areaOfWork,
        String vehicleType,
        BigDecimal rating,
        Boolean isAvailable,
        Boolean isActive
) {
    public static CourierResponse fromEntity(Courier courier) {
        if (courier == null) return null;
        return new CourierResponse(
                courier.getId(),
                courier.getFullName(),
                courier.getPhone(),
                courier.getEmail(),
                courier.getEmployeeDate(),
                courier.getAreaOfWork(),
                courier.getVehicleType().name(),
                courier.getRating(),
                courier.getIsAvailable(),
                courier.getIsActive()
        );
    }
}