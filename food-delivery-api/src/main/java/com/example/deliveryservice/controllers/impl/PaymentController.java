package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.response.PaymentResponse;
import com.example.deliveryservice.entity.PaymentMethod;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Платежи", description = """
        Endpoints для обработки платежей. \
        Включает создание записи о платеже, подтверждение, обработку статусов успеха и неудачи.
        """)
@SecurityRequirement(name = "bearer-auth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
        summary = "Создать платеж",
        description = "Инициация записи о платеже для заказа. Требует ID заказа и метод оплаты. Платеж проходит через стадии: pending, processing, completed или failed. Доступно клиенту-владельцу заказа и системным администраторам."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@RequestParam UUID orderId,
                                  @RequestParam PaymentMethod paymentMethod,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        return PaymentResponse.fromEntity(paymentService.create(orderId, paymentMethod, currentUser));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить платеж",
        description = "Возвращает информацию о платеже включая статус, метод оплаты и идентификатор транзакции. Доступно клиенту-владельцу заказа, администратору ресторана и системным администраторам."
    )
    public PaymentResponse getById(@PathVariable UUID id,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        return PaymentResponse.fromEntity(paymentService.getById(id, currentUser));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Получить платеж заказа",
        description = "Возвращает платежную информацию для конкретного заказа. Доступно клиенту-владельцу заказа, администратору ресторана и системным администраторам."
    )
    public PaymentResponse getByOrder(@PathVariable UUID orderId,
                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        return PaymentResponse.fromEntity(paymentService.getByOrderId(orderId, currentUser));
    }

    @PatchMapping("/{id}/process")
    @Operation(
        summary = "Обработать платеж",
        description = "Отметка о начале обработки платежа платежным шлюзом с указанием идентификатора внешней транзакции. Доступно только системным администраторам."
    )
    public PaymentResponse process(@PathVariable UUID id,
                                   @RequestParam String externalTransactionId,
                                   @RequestParam String gateway,
                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        return PaymentResponse.fromEntity(paymentService.process(id, externalTransactionId, gateway, currentUser));
    }

    @PatchMapping("/{id}/complete")
    @Operation(
        summary = "Завершить платеж",
        description = "Подтверждение успешного завершения платежа. Платеж переходит в статус 'completed'. Доступно только системным администраторам."
    )
    public PaymentResponse complete(@PathVariable UUID id,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        return PaymentResponse.fromEntity(paymentService.complete(id, currentUser));
    }

    @PatchMapping("/{id}/fail")
    @Operation(
        summary = "Пометить как неудачный",
        description = "Отметка о неудаче платежа с указанием причины. Платеж переходит в статус 'failed'. Доступно только системным администраторам."
    )
    public PaymentResponse fail(@PathVariable UUID id,
                                @RequestParam String errorMessage,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        return PaymentResponse.fromEntity(paymentService.fail(id, errorMessage, currentUser));
    }
}
