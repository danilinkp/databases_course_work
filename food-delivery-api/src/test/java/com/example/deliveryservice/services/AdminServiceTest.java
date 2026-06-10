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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private AdminRepository adminRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    void ownsRestaurant_delegatesToRepository() {
        UUID adminId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        when(adminRepository.ownsRestaurant(adminId, restaurantId)).thenReturn(true);

        assertThat(adminService.ownsRestaurant(adminId, restaurantId)).isTrue();
    }

    @Test
    void registerAdmin_duplicateEmail_isRejected() {
        AdminRegisterCommand command = new AdminRegisterCommand("a@e.com", "pass", "system_admin", null);
        when(adminRepository.existsByEmail("a@e.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.registerAdmin(command))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void registerRestaurantAdmin_invalidToken_isRejected() {
        UUID restaurantId = UUID.randomUUID();
        RegisterRestaurantAdminCommand command = new RegisterRestaurantAdminCommand(
                "r@e.com", "pass", "WRONG-TOKEN", restaurantId);
        when(adminRepository.existsByEmail("r@e.com")).thenReturn(false);

        assertThatThrownBy(() -> adminService.registerRestaurantAdmin(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(restaurantRepository, never()).findById(any());
    }

    @Test
    void registerRestaurantAdmin_validToken_persistsAdmin() {
        UUID restaurantId = UUID.randomUUID();
        RegisterRestaurantAdminCommand command = new RegisterRestaurantAdminCommand(
                "r@e.com", "pass", "RESTAURANT-" + restaurantId, restaurantId);
        when(adminRepository.existsByEmail("r@e.com")).thenReturn(false);
        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.of(Restaurant.builder().id(restaurantId).build()));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(adminRepository.save(any(Admin.class))).thenAnswer(inv -> {
            Admin a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AdminResponse response = adminService.registerRestaurantAdmin(command);

        assertThat(response.email()).isEqualTo("r@e.com");
        assertThat(response.role()).isEqualTo(AdminRole.restaurant_admin.toString());
        assertThat(response.restaurantIds()).containsExactly(restaurantId);
    }

    @Test
    void getAdminsByRole_invalidRole_isRejected() {
        assertThatThrownBy(() -> adminService.getAdminsByRole("not_a_role"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAdminById_missing_isRejected() {
        UUID id = UUID.randomUUID();
        when(adminRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getAdminById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
