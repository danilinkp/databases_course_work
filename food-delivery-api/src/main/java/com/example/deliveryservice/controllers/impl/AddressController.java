package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.AddAddressCommand;
import com.example.deliveryservice.dto.command.UpdateAddressCommand;
import com.example.deliveryservice.dto.response.CustomerAddressResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.AddressService;
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
@RequestMapping("/api/v1/customers/{customerId}/addresses")
@RequiredArgsConstructor
@Tag(name = "Адреса", description = """
        Endpoints для управления адресами доставки клиентов. \
        Клиенты могут добавлять, редактировать, удалять адреса и устанавливать основной адрес для заказов.
        """)
@SecurityRequirement(name = "bearer-auth")
public class AddressController {

    private final AddressService customerAddressService;

    @PostMapping
    @Operation(
        summary = "Добавить адрес",
        description = "Сохранение нового адреса доставки для клиента. Включает регион, город, улицу, дом, квартиру, почтовый индекс, координаты и дополнительную информацию."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAddressResponse add(@PathVariable UUID customerId,
                                       @RequestBody @Valid AddAddressCommand command,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(customerId)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CustomerAddressResponse.fromEntity(
                customerAddressService.addAddress(customerId, command)
        );
    }

    @GetMapping
    @Operation(
        summary = "Получить все адреса",
        description = "Возвращает список всех сохранённых адресов клиента с указанием основного адреса."
    )
    public List<CustomerAddressResponse> getAll(@PathVariable UUID customerId,
                                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(customerId)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return customerAddressService.getByCustomerId(customerId).stream()
                .map(CustomerAddressResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{addressId}")
    @Operation(
        summary = "Получить адрес",
        description = "Возвращает детальную информацию о конкретном адресе доставки."
    )
    public CustomerAddressResponse getById(@PathVariable UUID customerId,
                                           @PathVariable UUID addressId,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(customerId)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CustomerAddressResponse.fromEntity(customerAddressService.getById(addressId));
    }

    @PatchMapping("/{addressId}")
    @Operation(
        summary = "Обновить адрес",
        description = "Редактирование существующего адреса доставки."
    )
    public CustomerAddressResponse update(@PathVariable UUID customerId,
                                          @PathVariable UUID addressId,
                                          @RequestBody @Valid UpdateAddressCommand command,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(customerId)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return CustomerAddressResponse.fromEntity(
                customerAddressService.update(addressId, command)
        );
    }

    @PatchMapping("/{addressId}/default")
    @Operation(
        summary = "Установить основной адрес",
        description = "Назначение адреса основным для быстрого выбора при оформлении заказа. С предыдущего основного адреса этот статус снимается."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setDefault(@PathVariable UUID customerId,
                           @PathVariable UUID addressId,
                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(customerId)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        customerAddressService.setDefault(customerId, addressId);
    }

    @DeleteMapping("/{addressId}")
    @Operation(
        summary = "Удалить адрес",
        description = "Удаление сохранённого адреса из списка клиентов."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID customerId,
                       @PathVariable UUID addressId,
                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!UUID.fromString(currentUser.getId()).equals(customerId)
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        customerAddressService.delete(addressId);
    }
}