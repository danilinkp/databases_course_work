package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.CreateDeliveryZoneCommand;
import com.example.deliveryservice.dto.command.CreateRestaurantCommand;
import com.example.deliveryservice.dto.command.UpdateRestaurantCommand;
import com.example.deliveryservice.dto.response.DeliveryZoneResponse;
import com.example.deliveryservice.dto.response.RestaurantResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.RestaurantService;
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
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Рестораны", description = """
        Endpoints для управления ресторанами. \
        Включает создание, обновление, деактивацию ресторанов, \
        а также управление зонами доставки.
        """)
@SecurityRequirement(name = "bearer-auth")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @Operation(
        summary = "Создать ресторан",
        description = "Регистрация нового ресторана в системе. Требует полную информацию о ресторане: название, адрес, почтовый индекс, географические координаты."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse create(@RequestBody @Valid CreateRestaurantCommand command) {
        return RestaurantResponse.fromEntity(restaurantService.create(command));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить ресторан по ID",
        description = "Возвращает полную информацию о ресторане включая базовую информацию, адрес и статус активности."
    )
    public RestaurantResponse getById(@PathVariable UUID id) {
        return RestaurantResponse.fromEntity(restaurantService.getById(id));
    }

    @GetMapping
    @Operation(
        summary = "Получить активные рестораны",
        description = "Возвращает список всех активных ресторанов, готовых принимать заказы."
    )
    public List<RestaurantResponse> getActive() {
        return restaurantService.getActive().stream()
                .map(RestaurantResponse::fromEntity)
                .toList();
    }

    @GetMapping("/by-postal-code")
    @Operation(
        summary = "Получить рестораны по почтовому коду",
        description = "Поиск ресторанов, осуществляющих доставку в указанный почтовый район."
    )
    public List<RestaurantResponse> getByPostalCode(@RequestParam String postalCode) {
        return restaurantService.getByPostalCode(postalCode).stream()
                .map(RestaurantResponse::fromEntity)
                .toList();
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Обновить данные ресторана",
        description = "Обновление информации о ресторане: название, адрес, статус активности. Доступно только администраторам данного ресторана."
    )
    public RestaurantResponse update(@PathVariable UUID id,
                                     @RequestBody @Valid UpdateRestaurantCommand command,
                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(id, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can update restaurant");
        }
        return RestaurantResponse.fromEntity(restaurantService.update(id, command));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Деактивировать ресторан",
        description = "Деактивация ресторана. Деактивированный ресторан перестаёт принимать новые заказы. Доступно только администраторам данного ресторана."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id,
                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(id, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can deactivate restaurant");
        }
        restaurantService.deactivate(id);
    }

    @PostMapping("/{id}/delivery-zones")
    @Operation(
        summary = "Добавить зону доставки",
        description = "Создание новой географической зоны доставки для ресторана. В зону входят почтовые индексы и параметры доставки."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryZoneResponse addZone(@PathVariable UUID id,
                                        @RequestBody @Valid CreateDeliveryZoneCommand command,
                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(id, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can add delivery zones");
        }
        return DeliveryZoneResponse.fromEntity(restaurantService.addDeliveryZone(id, command));
    }

    @GetMapping("/{id}/delivery-zones")
    @Operation(
        summary = "Получить зоны доставки",
        description = "Возвращает все географические зоны доставки ресторана с почтовыми индексами и параметрами."
    )
    public List<DeliveryZoneResponse> getZones(@PathVariable UUID id,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isRestaurantOwnerOrAdmin(id, currentUser)) {
            throw new AccessDeniedException("Only restaurant owners can view delivery zones");
        }
        return restaurantService.getDeliveryZones(id).stream()
                .map(DeliveryZoneResponse::fromEntity)
                .toList();
    }

    private boolean isRestaurantOwnerOrAdmin(UUID restaurantId, CustomUserDetails currentUser) {
        if (currentUser == null) {
            return false;
        }
        String role = currentUser.getRole();
        UUID currentUserId = UUID.fromString(currentUser.getId());

        // System admins can access everything
        if ("system_admin_role".equals(role)) {
            return true;
        }

        // Restaurant admins can only access their own restaurant
        if ("restaurant_admin_role".equals(role) && currentUserId.equals(restaurantId)) {
            return true;
        }

        return false;
    }
}