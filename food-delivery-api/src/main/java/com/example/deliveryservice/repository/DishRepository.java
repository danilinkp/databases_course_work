package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

    List<Dish> findByRestaurantId(UUID restaurantId);

    List<Dish> findByRestaurantIdAndIsAvailableTrue(UUID restaurantId);

    @Query("SELECT DISTINCT d FROM Dish d JOIN d.categories c WHERE c.id = :categoryId")
    List<Dish> findByCategoryId(UUID categoryId);
}
