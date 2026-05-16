package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DishCategoryRepository extends JpaRepository<DishCategory, UUID> {
}
