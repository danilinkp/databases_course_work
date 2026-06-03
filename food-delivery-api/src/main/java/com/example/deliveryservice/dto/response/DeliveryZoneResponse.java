package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.DeliveryZone;
import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryZoneResponse(
        UUID id,
        UUID restaurantId,
        String zoneName,
        String postalCode,
        BigDecimal deliveryFee,
        BigDecimal nearThreshold,
        BigDecimal farThreshold,
        BigDecimal feePerKm,
        BigDecimal peakSurcharge,
        BigDecimal weekendSurcharge,
        BigDecimal minOrderAmount,
        Integer deliveryTime
) {
    public static DeliveryZoneResponse fromEntity(DeliveryZone zone) {
        if (zone == null) return null;
        return new DeliveryZoneResponse(
                zone.getId(),
                zone.getRestaurant().getId(),
                zone.getZoneName(),
                zone.getPostalCode(),
                zone.getDeliveryFee(),
                zone.getNearThreshold(),
                zone.getFarThreshold(),
                zone.getFeePerKm(),
                zone.getPeakSurcharge(),
                zone.getWeekendSurcharge(),
                zone.getMinOrderAmount(),
                zone.getDeliveryTime()
        );
    }
}