package com.example.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "delivery_zones")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "near_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal nearThreshold = BigDecimal.valueOf(3.0);

    @Column(name = "far_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal farThreshold = BigDecimal.valueOf(5.0);

    @Column(name = "fee_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal feePerKm = BigDecimal.ZERO;

    @Column(name = "peak_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal peakSurcharge = BigDecimal.ZERO;

    @Column(name = "weekend_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal weekendSurcharge = BigDecimal.ZERO;

    @Column(name = "min_order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "delivery_time", nullable = false)
    private Integer deliveryTime;
}
