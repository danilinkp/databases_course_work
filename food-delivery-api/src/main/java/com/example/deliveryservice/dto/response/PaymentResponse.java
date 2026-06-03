package com.example.deliveryservice.dto.response;

import com.example.deliveryservice.entity.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        BigDecimal amount,
        String paymentMethod,
        String paymentStatus,
        String externalTransactionId,
        String paymentGateway,
        String errorMessage,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant processedAt
) {
    public static PaymentResponse fromEntity(Payment payment) {
        if (payment == null) return null;
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getPaymentStatus().name(),
                payment.getExternalTransactionId(),
                payment.getPaymentGateway(),
                payment.getErrorMessage(),
                payment.getMetadata(),
                payment.getCreatedAt(),
                payment.getProcessedAt()
        );
    }
}