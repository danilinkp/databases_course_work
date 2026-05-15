package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByCustomerId(UUID customerId);

    @Modifying
    @Query("UPDATE customer_addresses a SET a.is_default = false WHERE a.customer_id = :customerId")
    void clearDefaultForCustomer(UUID customerId);

}
