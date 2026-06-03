package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.*;
import com.example.deliveryservice.dto.response.CustomerResponse;
import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Клиенты", description = """
        Endpoints для управления аккаунтами покупателей. \
        Включает регистрацию, обновление профиля, управление бонусной программой, \
        смену пароля и управление аккаунтом.
        """)
@SecurityRequirement(name = "bearer-auth")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @Operation(
        summary = "Регистрация клиента",
        description = "Создание нового аккаунта покупателя. Клиент может делать заказы, управлять адресами доставки и оставлять отзывы о ресторанах."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@RequestBody @Valid RegisterCustomerCommand command) {
        return CustomerResponse.fromEntity(customerService.register(command));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить данные клиента",
        description = "Возвращает полную информацию о клиенте по ID. Доступно только самому клиенту или системным администраторам."
    )
    public CustomerResponse getById(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CustomerResponse.fromEntity(customerService.getById(id, currentUser));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Обновить данные клиента",
        description = "Обновление личной информации клиента: имя, телефон, email. Доступно только самому клиенту или системным администраторам."
    )
    public CustomerResponse update(@PathVariable UUID id,
                                   @RequestBody @Valid UpdateCustomerCommand command,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CustomerResponse.fromEntity(customerService.update(id, command, currentUser));
    }

    @PatchMapping("/{id}/password")
    @Operation(
        summary = "Изменить пароль",
        description = "Смена пароля аккаунта. Требует указание текущего пароля для верификации. Доступно только самому клиенту или системным администраторам."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable UUID id,
                               @RequestBody @Valid ChangePasswordCommand command,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        customerService.changePassword(id, command.oldPassword(), command.newPassword(), currentUser);
    }

    @PostMapping("/{id}/bonuses")
    @Operation(
        summary = "Начислить бонусы",
        description = "Административное начисление бонусных баллов на счёт клиента. Используется для компенсаций, акций и программ лояльности. Доступно только системным администраторам."
    )
    public CustomerResponse addBonuses(@PathVariable UUID id,
                                       @RequestParam int amount,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Only admins can add bonuses");
        }
        return CustomerResponse.fromEntity(customerService.addBonuses(id, amount, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить аккаунт",
        description = "Полное удаление аккаунта клиента и связанных данных. Доступно только самому клиенту или системным администраторам."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(id)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        customerService.delete(id, currentUser);
    }
}
