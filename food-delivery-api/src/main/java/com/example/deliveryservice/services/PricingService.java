package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.OrderTotal;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingService {

    private final EntityManager entityManager;

    public OrderTotal calculate(UUID orderId) {
        try {
            Object[] result = (Object[]) entityManager
                    .createNativeQuery("SELECT * FROM calculate_order_total(:orderId)")
                    .setParameter("orderId", orderId)
                    .getSingleResult();

            return new OrderTotal(
                    (BigDecimal) result[0],
                    (BigDecimal) result[1],
                    (BigDecimal) result[2],
                    (BigDecimal) result[3]
            );
        } catch (NoResultException e) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
    }
}
