package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.UpdateDeliveryZoneCommand;
import com.example.deliveryservice.dto.response.DeliveryZoneResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.DeliveryZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-zones")
@RequiredArgsConstructor
@Tag(name = "Зоны доставки", description = """
        Endpoints для управления географическими зонами доставки. \
        Зоны определяют районы куда осуществляется доставка. \
        """)
@SecurityRequirement(name = "bearer-auth")
public class DeliveryZoneController {

    private final DeliveryZoneService deliveryZoneService;

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить зону доставки",
        description = "Возвращает информацию о зоне доставки включая почтовые индексы, ограничения и условия."
    )
    public DeliveryZoneResponse getById(@PathVariable UUID id,
                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Delivery zones are public information
        return DeliveryZoneResponse.fromEntity(deliveryZoneService.getById(id));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Обновить зону доставки",
        description = "Обновление параметров зоны доставки: почтовые индексы, стоимость доставки, минимальная сумма заказа. Доступно администраторам соответствующего ресторана и системным администраторам."
    )
    public DeliveryZoneResponse update(@PathVariable UUID id,
                                       @RequestBody @Valid UpdateDeliveryZoneCommand command,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Delivery zones are admin managed - for restaurant admins, they need to own the zone's restaurant
        if (!isZoneAdmin(id, currentUser)) {
            throw new AccessDeniedException("Only admins can update delivery zones");
        }
        return DeliveryZoneResponse.fromEntity(deliveryZoneService.update(id, command));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить зону доставки",
        description = "Удаление зоны доставки из системы. Доступно администраторам соответствующего ресторана и системным администраторам."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Delivery zones are admin managed
        if (!isZoneAdmin(id, currentUser)) {
            throw new AccessDeniedException("Only admins can delete delivery zones");
        }
        deliveryZoneService.delete(id);
    }

    private boolean isZoneAdmin(UUID zoneId, CustomUserDetails currentUser) {
        String role = currentUser.getRole();
        UUID currentUserId = UUID.fromString(currentUser.getId());

        // System admins can manage all zones
        if ("system_admin_role".equals(role)) {
            return true;
        }

        // Restaurant admins can only manage zones for their own restaurant
        // We need to check the zone's restaurant_id against the user's restaurant_id
        // For now, we'll just check if the user is a system admin
        return false;
    }
}