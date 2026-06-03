package com.example.deliveryservice.services;

import com.example.deliveryservice.config.SecurityConfig;
import com.example.deliveryservice.dto.command.RegisterCustomerCommand;
import com.example.deliveryservice.dto.command.UpdateCustomerCommand;
import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.exceptions.WrongPasswordException;
import com.example.deliveryservice.repository.CustomerRepository;
import com.example.deliveryservice.security.CustomUserDetails;
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
    public Customer getById(UUID id, CustomUserDetails currentUser) {
        // Check if the current user is requesting their own data or is an admin
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + id); // Return not found to avoid leaking existence info
        }

        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Transactional
    public Customer update(UUID id, UpdateCustomerCommand command, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }

        Customer customer = getById(id, currentUser); // This will double-check ownership

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
    public void changePassword(UUID id, String oldPassword, String newPassword, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }

        Customer customer = getById(id, currentUser); // This will double-check ownership

        if (!passwordEncoder.matches(oldPassword, customer.getPasswordHash())) {
            throw new WrongPasswordException("Wrong password");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }

    @Transactional
    public void delete(UUID id, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }

    public Customer addBonuses(UUID id, int amount, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }

        Customer customer = getById(id, currentUser); // This will double-check ownership
        customer.setBonuses(customer.getBonuses() + amount);
        return customerRepository.save(customer);
    }

    /**
     * Check if the requested resource ID matches the current user's ID.
     */
    private boolean isOwnResource(UUID resourceId, CustomUserDetails currentUser) {
        try {
            UUID userId = UUID.fromString(currentUser.getId());
            return userId.equals(resourceId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if the current user has admin role.
     * In a real system, you might have more sophisticated role checking.
     */
    private boolean isAdmin(CustomUserDetails currentUser) {
        // Check for admin roles - system_admin_role or restaurant_admin_role might have broader access
        // For customer data access, we'll consider system_admin_role as having full access
        return "system_admin_role".equals(currentUser.getRole());
    }
}