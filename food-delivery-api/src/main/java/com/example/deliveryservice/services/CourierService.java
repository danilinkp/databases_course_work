package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.RegisterCourierCommand;
import com.example.deliveryservice.dto.command.UpdateCourierCommand;
import com.example.deliveryservice.entity.Courier;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.exceptions.WrongPasswordException;
import com.example.deliveryservice.repository.CourierRepository;
import com.example.deliveryservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourierService {
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Courier register(RegisterCourierCommand command) {
        if (courierRepository.existsByEmail(command.email())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (courierRepository.existsByPhone(command.phone())) {
            throw new ResourceAlreadyExistsException("Phone already exists");
        }

        Courier courier = Courier.builder()
                .fullName(command.fullName())
                .email(command.email())
                .phone(command.phone())
                .passwordHash(passwordEncoder.encode(command.password()))
                .employeeDate(LocalDate.now())
                .areaOfWork(command.areaOfWork())
                .vehicleType(command.vehicleType())
                .rating(BigDecimal.ZERO)
                .isAvailable(true)
                .isActive(true)
                .build();

        return courierRepository.save(courier);
    }

    @Transactional(readOnly = true)
    public Courier getById(UUID id, CustomUserDetails currentUser) {
        // Check if the current user is requesting their own data or is an admin
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Courier not found: " + id); // Return not found to avoid leaking existence info
        }

        return courierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found: " + id));
    }

    @Transactional
    public Courier update(UUID id, UpdateCourierCommand command, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Courier not found: " + id);
        }

        Courier courier = getById(id, currentUser); // This will double-check ownership

        if (command.fullName() != null) courier.setFullName(command.fullName());
        if (command.areaOfWork() != null) courier.setAreaOfWork(command.areaOfWork());
        if (command.vehicleType() != null) courier.setVehicleType(command.vehicleType());

        if (command.phone() != null && !command.phone().equals(courier.getPhone())) {
            if (courierRepository.existsByPhone(command.phone())) {
                throw new ResourceAlreadyExistsException("Phone already exists");
            }
            courier.setPhone(command.phone());
        }
        if (command.email() != null && !command.email().equals(courier.getEmail())) {
            if (courierRepository.existsByEmail(command.email())) {
                throw new ResourceAlreadyExistsException("Email already exists");
            }
            courier.setEmail(command.email());
        }

        return courierRepository.save(courier);
    }

    @Transactional
    public void changePassword(UUID id, String oldPassword, String newPassword, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Courier not found: " + id);
        }

        Courier courier = getById(id, currentUser); // This will double-check ownership

        if (!passwordEncoder.matches(oldPassword, courier.getPasswordHash())) {
            throw new WrongPasswordException("Wrong password");
        }

        courier.setPasswordHash(passwordEncoder.encode(newPassword));
        courierRepository.save(courier);
    }

    public Courier setAvailability(UUID id, boolean isAvailable, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Courier not found: " + id);
        }

        Courier courier = getById(id, currentUser); // This will double-check ownership
        courier.setIsAvailable(isAvailable);
        return courierRepository.save(courier);
    }

    public void deactivate(UUID id, CustomUserDetails currentUser) {
        // Check ownership
        if (!isOwnResource(id, currentUser) && !isAdmin(currentUser)) {
            throw new ResourceNotFoundException("Courier not found: " + id);
        }

        Courier courier = getById(id, currentUser); // This will double-check ownership
        courier.setIsActive(false);
        courier.setIsAvailable(false);
        courierRepository.save(courier);
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
     * For courier data, we'll consider system_admin_role as having full access.
     * Restaurant admins might also have access to couriers in their area? But for simplicity, we'll stick to system admin.
     */
    private boolean isAdmin(CustomUserDetails currentUser) {
        // Check for admin roles - system_admin_role has full access
        return "system_admin_role".equals(currentUser.getRole());
    }
}