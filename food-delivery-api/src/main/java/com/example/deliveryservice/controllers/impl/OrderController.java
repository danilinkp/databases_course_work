package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.CreateOrderCommand;
import com.example.deliveryservice.dto.response.OrderResponse;
import com.example.deliveryservice.entity.Order;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.OrderService;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы", description = """
        Endpoints для управления заказами. \
        Включает создание заказов, подтверждение, управление статусами (подтверждение, приготовление, готовность, доставка), \
        отмену заказов и назначение курьеров.
        """)
@SecurityRequirement(name = "bearer-auth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
        summary = "Создать заказ",
        description = "Создание нового заказа на доставку. Клиент выбирает блюда из меню ресторана, указывает адрес доставки и специальные пожелания. После создания заказ получает статус 'pending' и ожидает подтверждения рестораном."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrderCommand command,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"customer_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Only customers can create orders");
        }
        UUID customerId = UUID.fromString(currentUser.getId());
        Order order = orderService.create(customerId, command, currentUser);
        return toResponse(order);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить заказ по ID",
        description = "Возвращает полную информацию о заказе включая состав заказа, статус, сумму и адрес доставки. Доступно клиентам (их собственные заказы), ресторанам (их заказы), курьерам (их доставка) и администраторам."
    )
    public OrderResponse getById(@PathVariable UUID id,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        Order order = orderService.getById(id, currentUser);
        return toResponse(order);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Получить заказы клиента",
        description = "Возвращает историю всех заказов клиента с их статусами и суммами. Доступно только самому клиенту или системным администраторам."
    )
    public List<OrderResponse> getByCustomer(@PathVariable UUID customerId,
                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!("customer_role".equals(currentUser.getRole()) &&
                UUID.fromString(currentUser.getId()).equals(customerId))
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return orderService.getByCustomerId(customerId, currentUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(
        summary = "Получить заказы ресторана",
        description = "Возвращает все заказы ресторана для обработки. Используется персоналом ресторана для управления процессом приготовления. Доступно администраторам ресторана и системным администраторам."
    )
    public List<OrderResponse> getByRestaurant(@PathVariable UUID restaurantId,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return orderService.getByRestaurantId(restaurantId, currentUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/confirm")
    @Operation(
        summary = "Подтвердить заказ",
        description = "Подтверждение заказа рестораном. После подтверждения заказ переходит в статус 'confirmed' и начинается процесс приготовления. Доступно администраторам ресторана и системным администраторам."
    )
    public OrderResponse confirm(@PathVariable UUID id,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(orderService.confirm(id, currentUser));
    }

    @PatchMapping("/{id}/preparing")
    @Operation(
        summary = "Начать приготовление",
        description = "Отметка о начале приготовления заказа. Переводит заказ в статус 'preparing'. Доступно администраторам ресторана и системным администраторам."
    )
    public OrderResponse startPreparing(@PathVariable UUID id,
                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(orderService.startPreparing(id, currentUser));
    }

    @PatchMapping("/{id}/ready")
    @Operation(
        summary = "Отметить заказ как готовый",
        description = "Отметка о готовности заказа к выдаче курьеру. Переводит заказ в статус 'ready'. После этого заказ можно назначить курьеру. Доступно администраторам ресторана и системным администраторам."
    )
    public OrderResponse markReady(@PathVariable UUID id,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(orderService.markReady(id, currentUser));
    }

    @PatchMapping("/{id}/assign-courier")
    @Operation(
        summary = "Назначить курьера",
        description = "Назначение доступного курьера на заказ для доставки. Заказ должен быть в статусе 'ready'. Доступно администраторам ресторана и системным администраторам."
    )
    public OrderResponse assignCourier(@PathVariable UUID id,
                                       @RequestParam UUID courierId,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(orderService.assignCourier(id, courierId, currentUser));
    }

    @PatchMapping("/{id}/complete")
    @Operation(
        summary = "Завершить доставку",
        description = "Отметка об успешной доставке заказа клиенту. Переводит заказ в финальный статус 'delivered'. После завершения заказ можно оценить отзывом. Доступно курьерам, администраторам ресторана и системным администраторам."
    )
    public OrderResponse completeDelivery(@PathVariable UUID id,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"courier_role".equals(currentUser.getRole())
                && !"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(orderService.completeDelivery(id, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "Отменить заказ",
        description = "Отмена заказа. Доступна клиенту-владельцу и системным администраторам, пока заказ не передан на доставку. Заказ переходит в статус 'cancelled'."
    )
    public OrderResponse cancel(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!"customer_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return toResponse(orderService.cancel(id, currentUser));
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.fromEntity(order, orderService.getItems(order.getId()));
    }
}