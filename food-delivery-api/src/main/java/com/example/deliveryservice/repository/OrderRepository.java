package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerCustomerId(UUID customerId);

    List<Order> findByRestaurantId(UUID restaurantId);

}
