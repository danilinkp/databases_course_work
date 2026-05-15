package com.example.deliveryservice.services;

import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.exceptions.WrongPasswordException;
import com.example.deliveryservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public Customer register(String fullName, String email, String phone, String password) {
        if (customerRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (customerRepository.existsByPhone(phone)) {
            throw new ResourceAlreadyExistsException("Phone already exists");
        }

        Customer customer = Customer.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .bonuses(0)
                .isActive(true)
                .build();

        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public Customer update(UUID id, String fullName, String email, String phone) {
        Customer customer = getById(id);

        if (phone != null && !phone.equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(phone)) {
                throw new ResourceAlreadyExistsException("Phone already exists");
            }
            customer.setPhone(phone);
        }
        if (email != null && !email.equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(email)) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
            customer.setEmail(email);
        }
        if (fullName != null) {
            customer.setFullName(fullName);
        }

        return customerRepository.save(customer);
    }

    public void changePassword(UUID id, String oldPassword, String newPassword) {
        Customer customer = getById(id);

        if (!passwordEncoder.matches(oldPassword, customer.getPasswordHash())) {
            throw new WrongPasswordException("Wrong password");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }

    public void delete(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }

    public Customer addBonuses(UUID id, int amount) {
        Customer customer = getById(id);
        customer.setBonuses(customer.getBonuses() + amount);
        return customerRepository.save(customer);
    }


}
