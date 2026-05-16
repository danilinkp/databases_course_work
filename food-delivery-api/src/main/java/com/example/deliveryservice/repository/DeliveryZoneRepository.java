package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {

    List<DeliveryZone> findByRestaurantId(UUID restaurantId);

    boolean existsByRestaurantIdAndPostalCode(UUID restaurantId, String postalCode);

    Optional<DeliveryZone> findByRestaurantIdAndPostalCode(UUID restaurantId, String postalCode);

    Optional<DeliveryZone> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
