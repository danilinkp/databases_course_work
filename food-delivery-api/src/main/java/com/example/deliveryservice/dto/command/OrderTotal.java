package com.example.deliveryservice.dto.command;

import java.math.BigDecimal;

public record OrderTotal(
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal discount,
        BigDecimal totalAmount

) {
}
