package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Admin;
import com.example.deliveryservice.entity.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT a FROM Admin a JOIN a.restaurants r WHERE r.id = :restaurantId")
    List<Admin> findByRestaurantsContainingId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT COUNT(a) > 0 FROM Admin a JOIN a.restaurants r WHERE a.id = :adminId AND r.id = :restaurantId")
    boolean ownsRestaurant(@Param("adminId") UUID adminId, @Param("restaurantId") UUID restaurantId);

    List<Admin> findByRole(AdminRole role);
}