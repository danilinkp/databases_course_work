package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Restaurant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query("SELECT DISTINCT r FROM restaurants r JOIN delivery_zones dz ON dz.restaurant = r " +
            "WHERE dz.postal_code = :postalCode AND r.is_active = true")
    List<Restaurant> findByPostalCode(@Param("postalCode") String postalCode);

    List<Restaurant> findByIsActiveTrue();

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
