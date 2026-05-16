package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
