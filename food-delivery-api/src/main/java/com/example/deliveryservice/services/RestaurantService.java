package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.CreateDeliveryZoneCommand;
import com.example.deliveryservice.dto.command.CreateRestaurantCommand;
import com.example.deliveryservice.dto.command.UpdateRestaurantCommand;
import com.example.deliveryservice.entity.DeliveryZone;
import com.example.deliveryservice.entity.Restaurant;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.DeliveryZoneRepository;
import com.example.deliveryservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;

    public Restaurant create(CreateRestaurantCommand command) {
        if (restaurantRepository.existsByEmail(command.email())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (restaurantRepository.existsByPhone(command.phone())) {
            throw new ResourceAlreadyExistsException("Phone already exists");
        }

        Restaurant restaurant = Restaurant.builder()
                .name(command.name())
                .cuisineType(command.cuisineType())
                .address(command.address())
                .phone(command.phone())
                .email(command.email())
                .latitude(BigDecimal.valueOf(command.latitude()))
                .longitude(BigDecimal.valueOf(command.longitude()))
                .openingTime(command.openingTime())
                .closingTime(command.closingTime())
                .rating(BigDecimal.ZERO)
                .isActive(true)
                .build();

        return restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant getById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Restaurant> getActive() {
        return restaurantRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Restaurant> getByPostalCode(String postalCode) {
        return restaurantRepository.findByPostalCode(postalCode);
    }

    public Restaurant update(UUID id, UpdateRestaurantCommand command) {
        Restaurant restaurant = getById(id);

        if (command.name() != null) restaurant.setName(command.name());
        if (command.cuisineType() != null) restaurant.setCuisineType(command.cuisineType());
        if (command.address() != null) restaurant.setAddress(command.address());
        if (command.openingTime() != null) restaurant.setOpeningTime(command.openingTime());
        if (command.closingTime() != null) restaurant.setClosingTime(command.closingTime());
        if (command.latitude() != null) restaurant.setLatitude(BigDecimal.valueOf(command.latitude()));
        if (command.longitude() != null) restaurant.setLongitude(BigDecimal.valueOf(command.longitude()));

        if (command.phone() != null && !command.phone().equals(restaurant.getPhone())) {
            if (restaurantRepository.existsByPhone(command.phone())) {
                throw new ResourceAlreadyExistsException("Phone already exists");
            }
            restaurant.setPhone(command.phone());
        }
        if (command.email() != null && !command.email().equals(restaurant.getEmail())) {
            if (restaurantRepository.existsByEmail(command.email())) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
            restaurant.setEmail(command.email());
        }

        return restaurantRepository.save(restaurant);
    }

    public void deactivate(UUID id) {
        Restaurant restaurant = getById(id);
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }

    public DeliveryZone addDeliveryZone(UUID restaurantId, CreateDeliveryZoneCommand command) {
        Restaurant restaurant = getById(restaurantId);

        if (deliveryZoneRepository.existsByRestaurantIdAndPostalCode(restaurantId, command.postalCode())) {
            throw new ResourceAlreadyExistsException("Zone with this postal code already exists");
        }

        DeliveryZone zone = DeliveryZone.builder()
                .restaurant(restaurant)
                .zoneName(command.zoneName())
                .postalCode(command.postalCode())
                .deliveryFee(command.deliveryFee())
                .nearThreshold(command.nearThreshold())
                .farThreshold(command.farThreshold())
                .feePerKm(command.feePerKm())
                .peakSurcharge(command.peakSurcharge())
                .weekendSurcharge(command.weekendSurcharge())
                .minOrderAmount(command.minOrderAmount())
                .deliveryTime(command.deliveryTime())
                .build();

        return deliveryZoneRepository.save(zone);
    }

    @Transactional(readOnly = true)
    public List<DeliveryZone> getDeliveryZones(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        return deliveryZoneRepository.findByRestaurantId(restaurantId);
    }
}
