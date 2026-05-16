package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.CreateDeliveryZoneCommand;
import com.example.deliveryservice.dto.command.UpdateDeliveryZoneCommand;
import com.example.deliveryservice.entity.DeliveryZone;
import com.example.deliveryservice.entity.Restaurant;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.DeliveryZoneRepository;
import com.example.deliveryservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryZoneService {

    private final DeliveryZoneRepository deliveryZoneRepository;
    private final RestaurantRepository restaurantRepository;

    public DeliveryZone create(UUID restaurantId, CreateDeliveryZoneCommand command) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

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
    public DeliveryZone getById(UUID id) {
        return deliveryZoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery zone not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<DeliveryZone> getByRestaurantId(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        return deliveryZoneRepository.findByRestaurantId(restaurantId);
    }

    @Transactional(readOnly = true)
    public DeliveryZone getByRestaurantAndPostalCode(UUID restaurantId, String postalCode) {
        return deliveryZoneRepository.findByRestaurantIdAndPostalCode(restaurantId, postalCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery zone not found for restaurant " + restaurantId
                                + " and postal code " + postalCode));
    }

    public DeliveryZone update(UUID id, UpdateDeliveryZoneCommand command) {
        DeliveryZone zone = getById(id);

        if (command.zoneName() != null) zone.setZoneName(command.zoneName());
        if (command.deliveryFee() != null) zone.setDeliveryFee(command.deliveryFee());
        if (command.nearThreshold() != null) zone.setNearThreshold(command.nearThreshold());
        if (command.farThreshold() != null) zone.setFarThreshold(command.farThreshold());
        if (command.feePerKm() != null) zone.setFeePerKm(command.feePerKm());
        if (command.peakSurcharge() != null) zone.setPeakSurcharge(command.peakSurcharge());
        if (command.weekendSurcharge() != null) zone.setWeekendSurcharge(command.weekendSurcharge());
        if (command.minOrderAmount() != null) zone.setMinOrderAmount(command.minOrderAmount());
        if (command.deliveryTime() != null) zone.setDeliveryTime(command.deliveryTime());

        if (command.postalCode() != null && !command.postalCode().equals(zone.getPostalCode())) {
            if (deliveryZoneRepository.existsByRestaurantIdAndPostalCode(
                    zone.getRestaurant().getId(), command.postalCode())) {
                throw new ResourceAlreadyExistsException("Zone with this postal code already exists");
            }
            zone.setPostalCode(command.postalCode());
        }

        return deliveryZoneRepository.save(zone);
    }

    public void delete(UUID id) {
        if (!deliveryZoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Delivery zone not found: " + id);
        }
        deliveryZoneRepository.deleteById(id);
    }
}