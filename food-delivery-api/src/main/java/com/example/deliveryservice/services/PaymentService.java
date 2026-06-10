package com.example.deliveryservice.services;

import com.example.deliveryservice.entity.*;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.AdminRepository;
import com.example.deliveryservice.repository.OrderRepository;
import com.example.deliveryservice.repository.PaymentRepository;
import com.example.deliveryservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AdminRepository adminRepository;

    public Payment create(UUID orderId, PaymentMethod paymentMethod, CustomUserDetails currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isOrderCustomer(order, currentUser) && !isSystemAdmin(currentUser)) {
            throw new AccessDeniedException("Only the order customer can pay for the order");
        }

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
    public Payment getById(UUID id, CustomUserDetails currentUser) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));

        if (!canAccessPayment(payment, currentUser)) {
            throw new AccessDeniedException("Access denied");
        }
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(UUID orderId, CustomUserDetails currentUser) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        if (!canAccessPayment(payment, currentUser)) {
            throw new AccessDeniedException("Access denied");
        }
        return payment;
    }

    public Payment process(UUID paymentId, String externalTransactionId, String gateway, CustomUserDetails currentUser) {
        Payment payment = getProcessablePayment(paymentId, currentUser);

        if (payment.getPaymentStatus() != PaymentStatus.pending) {
            throw new IllegalStateException("Payment is not in pending state");
        }

        payment.setExternalTransactionId(externalTransactionId);
        payment.setPaymentGateway(gateway);
        payment.setPaymentStatus(PaymentStatus.processing);
        payment.setProcessedAt(Instant.now());

        return paymentRepository.save(payment);
    }

    public Payment complete(UUID paymentId, CustomUserDetails currentUser) {
        Payment payment = getProcessablePayment(paymentId, currentUser);

        if (payment.getPaymentStatus() != PaymentStatus.processing) {
            throw new IllegalStateException("Payment is not in processing state");
        }

        payment.setPaymentStatus(PaymentStatus.completed);
        payment.setCompletedAt(Instant.now());

        return paymentRepository.save(payment);
    }

    public Payment fail(UUID paymentId, String errorMessage, CustomUserDetails currentUser) {
        Payment payment = getProcessablePayment(paymentId, currentUser);

        if (payment.getPaymentStatus() == PaymentStatus.completed) {
            throw new IllegalStateException("Cannot fail a completed payment");
        }

        payment.setPaymentStatus(PaymentStatus.failed);
        payment.setErrorMessage(errorMessage);

        return paymentRepository.save(payment);
    }

    private Payment getProcessablePayment(UUID paymentId, CustomUserDetails currentUser) {
        if (!isSystemAdmin(currentUser)) {
            throw new AccessDeniedException("Only system administrators can manage payment processing");
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
    }

    private boolean canAccessPayment(Payment payment, CustomUserDetails currentUser) {
        Order order = payment.getOrder();
        if (isSystemAdmin(currentUser) || isOrderCustomer(order, currentUser)) {
            return true;
        }
        if ("restaurant_admin_role".equals(currentUser.getRole())) {
            return adminRepository.ownsRestaurant(UUID.fromString(currentUser.getId()), order.getRestaurant().getId());
        }
        return false;
    }

    private boolean isOrderCustomer(Order order, CustomUserDetails currentUser) {
        if (!"customer_role".equals(currentUser.getRole())) {
            return false;
        }
        try {
            return order.getCustomer().getId().equals(UUID.fromString(currentUser.getId()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isSystemAdmin(CustomUserDetails currentUser) {
        return "system_admin_role".equals(currentUser.getRole());
    }
}
