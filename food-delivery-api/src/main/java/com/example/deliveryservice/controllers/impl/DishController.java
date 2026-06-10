package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.CreateDishCommand;
import com.example.deliveryservice.dto.command.UpdateDishCommand;
import com.example.deliveryservice.dto.response.DishResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.AdminService;
import com.example.deliveryservice.services.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/dishes")
@RequiredArgsConstructor
@Tag(name = "Блюда", description = """
        Endpoints для управления меню ресторана. \
        Включает создание, редактирование, удаление блюд, управление категориями и доступностью.
        """)
@SecurityRequirement(name = "bearer-auth")
public class DishController {

    private final DishService dishService;
    private final AdminService adminService;

    @PostMapping
    @Operation(
        summary = "Создать блюдо",
        description = "Добавление нового блюда в меню ресторана. Требует название, описание, цену и категорию. Доступно только администраторам ресторана."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public DishResponse create(@PathVariable UUID restaurantId,
                               @RequestBody @Valid CreateDishCommand command,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(restaurantId, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can add dishes");
        }
        return DishResponse.fromEntity(dishService.create(restaurantId, command));
    }

    @GetMapping
    @Operation(
        summary = "Получить все блюда",
        description = "Возвращает полный список блюд ресторана включая недоступные. Используется администраторами для управления меню. Доступно только администраторам ресторана."
    )
    public List<DishResponse> getAll(@PathVariable UUID restaurantId,
                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(restaurantId, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can view dishes");
        }
        return dishService.getByRestaurantId(restaurantId);
    }

    @GetMapping("/available")
    @Operation(
        summary = "Получить доступные блюда",
        description = "Возвращает список только доступных для заказа блюд ресторана. Этот метод публичный и используется клиентами для просмотра меню."
    )
    public List<DishResponse> getAvailable(@PathVariable UUID restaurantId) {
        return dishService.getAvailableByRestaurantId(restaurantId);
    }

    @GetMapping("/{dishId}")
    @Operation(
        summary = "Получить блюдо по ID",
        description = "Возвращает подробную информацию о конкретном блюде включая состав, калорийность и ингредиенты. Доступные блюда видны всем, включая гостей; снятые с продажи блюда доступны только администраторам ресторана."
    )
    public DishResponse getById(@PathVariable UUID restaurantId,
                                @PathVariable UUID dishId,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        DishResponse dish = dishService.getById(dishId);
        if (Boolean.FALSE.equals(dish.isAvailable()) && !isRestaurantOwnerOrAdmin(restaurantId, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can view unavailable dishes");
        }
        return dish;
    }

    @PatchMapping("/{dishId}")
    @Operation(
        summary = "Обновить блюдо",
        description = "Обновление информации о блюде: название, описание, цена, категория. Доступно только администраторам ресторана."
    )
    public DishResponse update(@PathVariable UUID restaurantId,
                               @PathVariable UUID dishId,
                               @RequestBody @Valid UpdateDishCommand command,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(restaurantId, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can update dishes");
        }
        return DishResponse.fromEntity(dishService.update(dishId, command));
    }

    @PatchMapping("/{dishId}/availability")
    @Operation(
        summary = "Изменить доступность блюда",
        description = "Включение или отключение блюда для заказа. Когда блюдо недоступно, оно не отображается в публичном меню но сохраняется в системе. Доступно только администраторам ресторана."
    )
    public DishResponse setAvailability(@PathVariable UUID restaurantId,
                                        @PathVariable UUID dishId,
                                        @RequestParam boolean available,
                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(restaurantId, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can change dish availability");
        }
        return DishResponse.fromEntity(dishService.setAvailability(dishId, available));
    }

    @DeleteMapping("/{dishId}")
    @Operation(
        summary = "Удалить блюдо",
        description = "Полное удаление блюда из меню ресторана. Используется для удаления устаревших или ошибочно созданных блюд. Доступно только администраторам ресторана."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID restaurantId,
                       @PathVariable UUID dishId,
                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(restaurantId, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can delete dishes");
        }
        dishService.delete(dishId);
    }

    private boolean isRestaurantOwnerOrAdmin(UUID restaurantId, CustomUserDetails currentUser) {
        if (currentUser == null) {
            return false;
        }
        String role = currentUser.getRole();

        if ("system_admin_role".equals(role)) {
            return true;
        }

        if ("restaurant_admin_role".equals(role)) {
            return adminService.ownsRestaurant(UUID.fromString(currentUser.getId()), restaurantId);
        }

        return false;
    }
}