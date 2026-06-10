package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.AdminRegisterCommand;
import com.example.deliveryservice.dto.command.RegisterRestaurantAdminCommand;
import com.example.deliveryservice.dto.response.AdminResponse;
import com.example.deliveryservice.entity.Admin;
import com.example.deliveryservice.entity.AdminRole;
import com.example.deliveryservice.entity.Restaurant;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.AdminRepository;
import com.example.deliveryservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String INVITATION_TOKEN_PREFIX = "RESTAURANT-";

    public AdminResponse registerAdmin(AdminRegisterCommand command) {
        if (adminRepository.existsByEmail(command.email())) {
            throw new ResourceAlreadyExistsException("Admin with email " + command.email() + " already exists");
        }

        AdminRole role = AdminRole.valueOf(command.role().replace(" ", "_").toLowerCase());

        Admin admin = Admin.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .role(role)
                .isActive(true)
                .restaurants(new HashSet<>())
                .build();

        if (command.restaurantIds() != null && !command.restaurantIds().isEmpty()) {
            List<Restaurant> restaurants = restaurantRepository.findAllById(
                    command.restaurantIds().stream()
                            .map(id -> UUID.fromString(id))
                            .collect(Collectors.toList())
            );
            admin.getRestaurants().addAll(restaurants);
        }

        Admin saved = adminRepository.save(admin);
        return mapToResponse(saved);
    }

    public AdminResponse registerRestaurantAdmin(RegisterRestaurantAdminCommand command) {
        if (adminRepository.existsByEmail(command.email())) {
            throw new ResourceAlreadyExistsException("Admin with email " + command.email() + " already exists");
        }

        if (!validateInvitationToken(command.invitationToken(), command.restaurantId())) {
            throw new IllegalArgumentException("Invalid invitation token");
        }

        Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + command.restaurantId()));

        Admin admin = Admin.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .role(AdminRole.restaurant_admin)
                .isActive(true)
                .restaurants(Set.of(restaurant))
                .build();

        Admin saved = adminRepository.save(admin);
        return mapToResponse(saved);
    }

    private boolean validateInvitationToken(String token, UUID restaurantId) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String expectedToken = INVITATION_TOKEN_PREFIX + restaurantId.toString();
        return token.equals(expectedToken);
    }

    @Transactional(readOnly = true)
    public boolean ownsRestaurant(UUID adminId, UUID restaurantId) {
        return adminRepository.ownsRestaurant(adminId, restaurantId);
    }

    public AdminResponse getAdminById(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return mapToResponse(admin);
    }

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AdminResponse> getAdminsByRole(String role) {
        try {
            AdminRole adminRole = AdminRole.valueOf(role.replace(" ", "_").toLowerCase());
            return adminRepository.findByRole(adminRole).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid role: " + role);
        }
    }

    public List<AdminResponse> getAdminsByRestaurantId(UUID restaurantId) {
        return adminRepository.findByRestaurantsContainingId(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AdminResponse updateAdmin(UUID id, AdminRegisterCommand command) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        if (!command.email().equals(admin.getEmail()) && adminRepository.existsByEmail(command.email())) {
            throw new ResourceAlreadyExistsException("Admin with email " + command.email() + " already exists");
        }

        admin.setEmail(command.email());
        if (command.password() != null && !command.password().isEmpty()) {
            admin.setPasswordHash(passwordEncoder.encode(command.password()));
        }
        admin.setRole(AdminRole.valueOf(command.role().replace(" ", "_").toLowerCase()));

        if (command.restaurantIds() != null) {
            admin.getRestaurants().clear();
            if (!command.restaurantIds().isEmpty()) {
                List<Restaurant> restaurants = restaurantRepository.findAllById(
                        command.restaurantIds().stream()
                                .map(rid -> UUID.fromString(rid))
                                .collect(Collectors.toList())
                );
                admin.getRestaurants().addAll(restaurants);
            }
        }

        return mapToResponse(adminRepository.save(admin));
    }

    public void deleteAdmin(UUID id) {
        if (!adminRepository.existsById(id)) {
            throw new ResourceNotFoundException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }

    private AdminResponse mapToResponse(Admin admin) {
        List<UUID> restaurantIds = admin.getRestaurants().stream()
                .map(Restaurant::getId)
                .collect(Collectors.toList());

        return new AdminResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getRole().toString(),
                restaurantIds,
                admin.getIsActive(),
                admin.getCreatedAt()
        );
    }
}