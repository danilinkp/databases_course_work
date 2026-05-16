package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderId(UUID orderId);

    Optional<Review> findByOrderId(UUID uuid);

    List<Review> findByRestaurantId(UUID restaurantId);
}
