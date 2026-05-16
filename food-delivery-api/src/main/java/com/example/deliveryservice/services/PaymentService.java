package com.example.deliveryservice.services;

import com.example.deliveryservice.entity.*;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.OrderRepository;
import com.example.deliveryservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment create(UUID orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.cancelled) {
            throw new IllegalStateException("Cannot create payment for cancelled order");
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new ResourceAlreadyExistsException("Payment already exists for order: " + orderId);
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.pending)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
    }

    public Payment process(UUID paymentId, String externalTransactionId, String gateway) {
        Payment payment = getById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.pending) {
            throw new IllegalStateException("Payment is not in pending state");
        }

        payment.setExternalTransactionId(externalTransactionId);
        payment.setPaymentGateway(gateway);
        payment.setPaymentStatus(PaymentStatus.processing);
        payment.setProcessedAt(Instant.from(LocalDateTime.now()));

        return paymentRepository.save(payment);
    }

    public Payment complete(UUID paymentId) {
        Payment payment = getById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.processing) {
            throw new IllegalStateException("Payment is not in processing state");
        }

        payment.setPaymentStatus(PaymentStatus.completed);
        payment.setCompletedAt(Instant.from(LocalDateTime.now()));

        return paymentRepository.save(payment);
    }

    public Payment fail(UUID paymentId, String errorMessage) {
        Payment payment = getById(paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.completed) {
            throw new IllegalStateException("Cannot fail a completed payment");
        }

        payment.setPaymentStatus(PaymentStatus.failed);
        payment.setErrorMessage(errorMessage);

        return paymentRepository.save(payment);
    }
}
