package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.CreateDishCommand;
import com.example.deliveryservice.dto.command.UpdateDishCommand;
import com.example.deliveryservice.entity.Dish;
import com.example.deliveryservice.entity.DishCategory;
import com.example.deliveryservice.entity.Restaurant;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.DishCategoryRepository;
import com.example.deliveryservice.repository.DishRepository;
import com.example.deliveryservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishCategoryRepository dishCategoryRepository;

    public Dish create(UUID restaurantId, CreateDishCommand command) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        DishCategory category = dishCategoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + command.categoryId()));

        Dish dish = Dish.builder()
                .restaurant(restaurant)
                .category(category)
                .name(command.name())
                .description(command.description())
                .price(command.price())
                .imageUrl(command.imageUrl())
                .isAvailable(true)
                .isSpicy(command.isSpicy() != null ? command.isSpicy() : false)
                .preparationTime(command.preparationTime())
                .calories(command.calories())
                .weightGrams(command.weightGrams())
                .build();

        return dishRepository.save(dish);
    }

    @Transactional(readOnly = true)
    public Dish getById(UUID id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Dish> getByRestaurantId(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        return dishRepository.findByRestaurantId(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<Dish> getAvailableByRestaurantId(UUID restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        return dishRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<Dish> getByCategoryId(UUID categoryId) {
        if (!dishCategoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        }
        return dishRepository.findByCategoryId(categoryId);
    }

    public Dish update(UUID id, UpdateDishCommand command) {
        Dish dish = getById(id);

        if (command.name() != null) dish.setName(command.name());
        if (command.description() != null) dish.setDescription(command.description());
        if (command.price() != null) dish.setPrice(command.price());
        if (command.imageUrl() != null) dish.setImageUrl(command.imageUrl());
        if (command.isSpicy() != null) dish.setIsSpicy(command.isSpicy());
        if (command.preparationTime() != null) dish.setPreparationTime(command.preparationTime());
        if (command.calories() != null) dish.setCalories(command.calories());
        if (command.weightGrams() != null) dish.setWeightGrams(command.weightGrams());

        if (command.categoryId() != null) {
            DishCategory category = dishCategoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + command.categoryId()));
            dish.setCategory(category);
        }

        return dishRepository.save(dish);
    }

    public Dish setAvailability(UUID id, boolean isAvailable) {
        Dish dish = getById(id);
        dish.setIsAvailable(isAvailable);
        return dishRepository.save(dish);
    }

    public void delete(UUID id) {
        if (!dishRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dish not found: " + id);
        }
        dishRepository.deleteById(id);
    }
}
