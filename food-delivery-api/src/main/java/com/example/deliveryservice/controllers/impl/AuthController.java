package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.AdminRegisterCommand;
import com.example.deliveryservice.dto.command.LoginCommand;
import com.example.deliveryservice.dto.command.RegisterCourierCommand;
import com.example.deliveryservice.dto.command.RegisterCustomerCommand;
import com.example.deliveryservice.dto.command.RegisterRestaurantAdminCommand;
import com.example.deliveryservice.dto.response.AdminResponse;
import com.example.deliveryservice.dto.response.CourierResponse;
import com.example.deliveryservice.dto.response.CustomerResponse;
import com.example.deliveryservice.dto.response.LoginResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.security.JwtTokenUtil;
import com.example.deliveryservice.services.AdminService;
import com.example.deliveryservice.services.CourierService;
import com.example.deliveryservice.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация и регистрация", description = """
        Endpoints для аутентификации пользователей и регистрации новых аккаунтов. \
        Поддерживается JWT аутентификация. \
        Доступна регистрация для клиентов, курьеров, системных администраторов и администраторов ресторанов.
        """)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomerService customerService;
    private final CourierService courierService;
    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему",
        description = "Аутентификация пользователя по email и паролю. Возвращает JWT токен доступа, роль пользователя и ID. Используется для получения доступа к защищённым endpoints."
    )
    public ResponseEntity<?> login(@RequestBody @Valid LoginCommand command) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.email(), command.password())
            );

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String token = jwtTokenUtil.generateToken(userDetails);

            if (userDetails instanceof CustomUserDetails customUserDetails) {
                return ResponseEntity.ok(new LoginResponse(
                        token,
                        customUserDetails.getRole(),
                        customUserDetails.getId()
                ));
            } else {
                return ResponseEntity.ok(new LoginResponse(
                        token,
                        "unknown",
                        "0"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Выход из системы",
        description = "Добавление JWT токена в blacklist (Redis) для аннулирования сессии. Требует заголовок Authorization с Bearer токеном."
    )
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            jwtTokenUtil.blacklistToken(token);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("Invalid authorization header");
    }

    @PostMapping("/register-customer")
    @Operation(
        summary = "Регистрация клиента",
        description = "Создание нового аккаунта покупателя. Клиенты могут делать заказы, управлять адресами доставки и оставлять отзывы."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse registerCustomer(@RequestBody @Valid RegisterCustomerCommand command) {
        return CustomerResponse.fromEntity(customerService.register(command));
    }

    @PostMapping("/register-courier")
    @Operation(
        summary = "Регистрация курьера",
        description = "Создание нового аккаунта курьера. Курьеры могут получать заказы на доставку и обновлять статус доступности."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CourierResponse registerCourier(@RequestBody @Valid RegisterCourierCommand command) {
        return CourierResponse.fromEntity(courierService.register(command));
    }

    @PostMapping("/register-system-admin")
    @Operation(
        summary = "Регистрация системного администратора",
        description = "Создание первого системного администратора для initial setup системы. Должен вызываться один раз при развертывании."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public AdminResponse registerSystemAdmin(@RequestBody @Valid AdminRegisterCommand command) {
        return adminService.registerAdmin(command);
    }

    @PostMapping("/register-restaurant-admin")
    @Operation(
        summary = "Регистрация администратора ресторана",
        description = "Саморегистрация администратора ресторана по коду приглашения. " +
                "Код приглашения выдаётся платформой при регистрации ресторана. " +
                "Формат invitationToken: RESTAURANT-{restaurantId}."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public AdminResponse registerRestaurantAdmin(@RequestBody @Valid RegisterRestaurantAdminCommand command) {
        return adminService.registerRestaurantAdmin(command);
    }
}