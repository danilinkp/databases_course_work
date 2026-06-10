package com.example.deliveryservice.security;

import com.example.deliveryservice.entity.Admin;
import com.example.deliveryservice.entity.AdminRole;
import com.example.deliveryservice.entity.Courier;
import com.example.deliveryservice.entity.Customer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void fromCustomer_exposesCustomerRoleAndAuthority() {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .email("c@e.com")
                .passwordHash("h")
                .isActive(true)
                .build();

        CustomUserDetails details = CustomUserDetails.fromCustomer(customer);

        assertThat(details.getRole()).isEqualTo("customer_role");
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_customer_role");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void fromCourier_exposesCourierRole() {
        Courier courier = Courier.builder()
                .id(UUID.randomUUID())
                .email("k@e.com")
                .passwordHash("h")
                .isActive(false)
                .build();

        CustomUserDetails details = CustomUserDetails.fromCourier(courier);

        assertThat(details.getRole()).isEqualTo("courier_role");
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_courier_role");
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void fromAdmin_systemAdmin_buildsMatchingAuthority() {
        Admin admin = Admin.builder()
                .id(UUID.randomUUID())
                .email("a@e.com")
                .passwordHash("h")
                .role(AdminRole.system_admin)
                .isActive(true)
                .build();

        CustomUserDetails details = CustomUserDetails.fromAdmin(admin);

        assertThat(details.getRole()).isEqualTo("system_admin_role");
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_system_admin_role");
    }

    @Test
    void fromAdmin_restaurantAdmin_buildsMatchingAuthority() {
        Admin admin = Admin.builder()
                .id(UUID.randomUUID())
                .email("ra@e.com")
                .passwordHash("h")
                .role(AdminRole.restaurant_admin)
                .isActive(true)
                .build();

        CustomUserDetails details = CustomUserDetails.fromAdmin(admin);

        assertThat(details.getRole()).isEqualTo("restaurant_admin_role");
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_restaurant_admin_role");
    }
}
