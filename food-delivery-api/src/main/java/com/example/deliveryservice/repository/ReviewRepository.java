package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderId(UUID orderId);

    Optional<Review> findByOrderId(UUID uuid);

    @Query("SELECT r FROM Review r JOIN r.order o WHERE o.restaurant.id = :restaurantId")
    List<Review> findByRestaurantId(@Param("restaurantId") UUID restaurantId);
}
