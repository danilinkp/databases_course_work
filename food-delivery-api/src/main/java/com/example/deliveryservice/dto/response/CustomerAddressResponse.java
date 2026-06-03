package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.CustomerAddress;
import java.math.BigDecimal;
import java.util.UUID;

public record CustomerAddressResponse(
        UUID id,
        UUID customerId,
        String region,
        String city,
        String street,
        String house,
        String apartment,
        String addressDetails,
        String postalCode,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean isDefault
) {
    public static CustomerAddressResponse fromEntity(CustomerAddress address) {
        if (address == null) return null;
        return new CustomerAddressResponse(
                address.getId(),
                address.getCustomer().getId(),
                address.getRegion(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment(),
                address.getAddressDetails(),
                address.getPostalCode(),
                address.getLatitude(),
                address.getLongitude(),
                address.getIsDefault()
        );
    }
}