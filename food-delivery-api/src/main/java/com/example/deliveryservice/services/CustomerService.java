package com.example.deliveryservice.services;

import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer register(String fullName, String email, String phone, String password) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (customerRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already exists");
        }

        Customer customer = Customer.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(password)
                .bonuses(0)
                .isActive(true)
                .build();

        return customerRepository.save(customer);
    }

    public Customer getCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
    }



}
