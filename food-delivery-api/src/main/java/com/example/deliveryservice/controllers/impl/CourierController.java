package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.ChangePasswordCommand;
import com.example.deliveryservice.dto.command.RegisterCourierCommand;
import com.example.deliveryservice.dto.command.UpdateCourierCommand;
import com.example.deliveryservice.dto.response.CourierResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.CourierService;
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
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
@Tag(name = "Курьеры", description = """
        Endpoints для управления курьерами. \
        Включает регистрацию, обновление профиля, управление статусом доступности \
        для получения заказов на доставку.
        """)
@SecurityRequirement(name = "bearer-auth")
public class CourierController {

    private final CourierService courierService;

    @PostMapping("/register")
    @Operation(
        summary = "Регистрация курьера",
        description = "Создание нового аккаунта курьера. Курьеры могут получать заказы на доставку, обновлять статус доступности и отслеживать историю доставок."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CourierResponse register(@RequestBody @Valid RegisterCourierCommand command) {
        return CourierResponse.fromEntity(courierService.register(command));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить данные курьера",
        description = "Возвращает полную информацию о курьере по ID. Доступно только самому курьеру или системным администраторам."
    )
    public CourierResponse getById(@PathVariable UUID id,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CourierResponse.fromEntity(courierService.getById(id, currentUser));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Обновить данные курьера",
        description = "Обновление личной информации курьера: имя, телефон, тип транспорта. Доступно только самому курьеру или системным администраторам."
    )
    public CourierResponse update(@PathVariable UUID id,
                                  @RequestBody @Valid UpdateCourierCommand command,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CourierResponse.fromEntity(courierService.update(id, command, currentUser));
    }

    @PatchMapping("/{id}/password")
    @Operation(
        summary = "Изменить пароль",
        description = "Смена пароля аккаунта курьера. Требует указание текущего пароля для верификации. Доступно только самому курьеру или системным администраторам."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable UUID id,
                               @RequestBody @Valid ChangePasswordCommand command,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        courierService.changePassword(id, command.oldPassword(), command.newPassword(), currentUser);
    }

    @PatchMapping("/{id}/availability")
    @Operation(
        summary = "Изменить статус доступности",
        description = "Переключение статуса доступности курьера для получения новых заказов. Когда курьер недоступен, ему не назначаются новые заказы."
    )
    public CourierResponse setAvailability(@PathVariable UUID id,
                                           @RequestParam boolean available,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CourierResponse.fromEntity(courierService.setAvailability(id, available, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Деактивировать курьера",
        description = "Деактивация аккаунта курьера. Деактивированный курьер не может получать новые заказы. Доступно только самому курьеру или системным администраторам."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id,
                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        courierService.deactivate(id, currentUser);
    }
}