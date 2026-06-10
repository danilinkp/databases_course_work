package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.AdminRegisterCommand;
import com.example.deliveryservice.dto.response.AdminResponse;
import com.example.deliveryservice.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@Tag(name = "Администраторы", description = """
        Endpoints для управления администраторами системы и ресторанов. \
        Системные администраторы могут создавать, обновлять и удалять аккаунты администраторов. \
        Администраторы ресторанов могут просматривать и изменять информацию о своих администраторах.
        """)
@SecurityRequirement(name = "bearer-auth")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('system_admin_role')")
    @Operation(
        summary = "Создать администратора",
        description = "Создание нового администратора системы или ресторана. Системные администраторы могут назначать роли и привязывать к ресторанам. Требует JWT токен системного администратора."
    )
    public ResponseEntity<AdminResponse> registerAdmin(@RequestBody @Valid AdminRegisterCommand command) {
        return new ResponseEntity<>(adminService.registerAdmin(command), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('system_admin_role') or hasRole('restaurant_admin_role')")
    @Operation(
        summary = "Получить администратора по ID",
        description = "Возвращает полную информацию об администраторе. Доступно системным администраторам и администраторам ресторана."
    )
    public ResponseEntity<AdminResponse> getAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('system_admin_role')")
    @Operation(
        summary = "Получить всех администраторов",
        description = "Возвращает полный список всех администраторов системы с их ролями и привязками к ресторанам. Доступно только системным администраторам."
    )
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('system_admin_role')")
    @Operation(
        summary = "Получить администраторов по роли",
        description = "Фильтрация администраторов по роли: system_admin или restaurant_admin. Доступно только системным администраторам."
    )
    public ResponseEntity<List<AdminResponse>> getAdminsByRole(@PathVariable String role) {
        return ResponseEntity.ok(adminService.getAdminsByRole(role));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('system_admin_role') or hasRole('restaurant_admin_role')")
    @Operation(
        summary = "Получить администраторов ресторана",
        description = "Возвращает список всех администраторов, имеющих доступ к указанному ресторану. Доступно системным и администраторам данного ресторана."
    )
    public ResponseEntity<List<AdminResponse>> getAdminsByRestaurant(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(adminService.getAdminsByRestaurantId(restaurantId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('system_admin_role')")
    @Operation(
        summary = "Обновить администратора",
        description = "Полное обновление информации об администраторе: email, пароль, роль, привязанные рестораны. Доступно только системным администраторам."
    )
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable UUID id,
            @RequestBody @Valid AdminRegisterCommand command) {
        return ResponseEntity.ok(adminService.updateAdmin(id, command));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('system_admin_role')")
    @Operation(
        summary = "Удалить администратора",
        description = "Полное удаление аккаунта администратора из системы. Доступно только системным администраторам."
    )
    public ResponseEntity<Void> deleteAdmin(@PathVariable UUID id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}