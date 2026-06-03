package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.response.PaymentResponse;
import com.example.deliveryservice.entity.PaymentMethod;
import com.example.deliveryservice.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        description = "Инициация записи о платеже для заказа. Требует заказ ID и метод оплаты. Платеж проходит через стадии:created, processing, completed или failed."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@RequestParam UUID orderId, @RequestParam PaymentMethod paymentMethod) {
        return PaymentResponse.fromEntity(paymentService.create(orderId, paymentMethod));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить платеж",
        description = "Возвращает информацию о платеже включая статус, метод оплаты и.getTransaction ID."
    )
    public PaymentResponse getById(@PathVariable UUID id) {
        return PaymentResponse.fromEntity(paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Получить платеж заказа",
        description = "Возвращает платежную информацию для конкретного заказа."
    )
    public PaymentResponse getByOrder(@PathVariable UUID orderId) {
        return PaymentResponse.fromEntity(paymentService.getByOrderId(orderId));
    }

    @PatchMapping("/{id}/process")
    @Operation(
        summary = "Обработать платеж",
        description = "Отметка о начале обработки платежа payment gateway с указанием external transaction ID."
    )
    public PaymentResponse process(@PathVariable UUID id, @RequestParam String externalTransactionId, @RequestParam String gateway) {
        return PaymentResponse.fromEntity(paymentService.process(id, externalTransactionId, gateway));
    }

    @PatchMapping("/{id}/complete")
    @Operation(
        summary = "Завершить платеж",
        description = "Подтверждение успешного completion платежа. Платеж переходит в статус 'completed'."
    )
    public PaymentResponse complete(@PathVariable UUID id) {
        return PaymentResponse.fromEntity(paymentService.complete(id));
    }

    @PatchMapping("/{id}/fail")
    @Operation(
        summary = "Пометить как неудачный",
        description = "Отметка о неудаче платежа с указанием причины. Платеж переходит в статус 'failed'.有关负责人 будет уведомлен."
    )
    public PaymentResponse fail(@PathVariable UUID id, @RequestParam String errorMessage) {
        return PaymentResponse.fromEntity(paymentService.fail(id, errorMessage));
    }
}
