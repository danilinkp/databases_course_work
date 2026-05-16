package com.example.deliveryservice.services;

import com.example.deliveryservice.config.SecurityConfig;
import com.example.deliveryservice.dto.command.RegisterCustomerCommand;
import com.example.deliveryservice.dto.command.UpdateCustomerCommand;
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

    @Transactional
    public Customer register(RegisterCustomerCommand command) {
        if (customerRepository.existsByEmail(command.email())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (customerRepository.existsByPhone(command.phone())) {
            throw new ResourceAlreadyExistsException("Phone already exists");
        }

        Customer customer = Customer.builder()
                .fullName(command.fullName())
                .email(command.email())
                .phone(command.phone())
                .passwordHash(passwordEncoder.encode(command.password()))
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

    @Transactional
    public Customer update(UUID id, UpdateCustomerCommand command) {
        Customer customer = getById(id);

        if (command.phone() != null && !command.phone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(command.phone())) {
                throw new ResourceAlreadyExistsException("Phone already exists");
            }
            customer.setPhone(command.phone());
        }
        if (command.email() != null && !command.email().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(command.email())) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
            customer.setEmail(command.email());
        }
        if (command.fullName() != null) {
            customer.setFullName(command.fullName());
        }

        return customerRepository.save(customer);
    }

    @Transactional
    public void changePassword(UUID id, String oldPassword, String newPassword) {
        Customer customer = getById(id);

        if (!passwordEncoder.matches(oldPassword, customer.getPasswordHash())) {
            throw new WrongPasswordException("Wrong password");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }

    @Transactional
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
