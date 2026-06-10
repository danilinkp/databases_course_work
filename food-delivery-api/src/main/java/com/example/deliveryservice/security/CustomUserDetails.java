package com.example.deliveryservice.security;

import com.example.deliveryservice.entity.Admin;
import com.example.deliveryservice.entity.Courier;
import com.example.deliveryservice.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final String role;
    private final String id;
    private final boolean active;

    public CustomUserDetails(String email, String password, String role, String id, boolean active) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.id = id;
        this.active = active;
    }

    public static CustomUserDetails fromCustomer(Customer customer) {
        return new CustomUserDetails(
                customer.getEmail(),
                customer.getPasswordHash(),
                "customer_role",
                customer.getId().toString(),
                customer.getIsActive()
        );
    }

    public static CustomUserDetails fromCourier(Courier courier) {
        return new CustomUserDetails(
                courier.getEmail(),
                courier.getPasswordHash(),
                "courier_role",
                courier.getId().toString(),
                courier.getIsActive()
        );
    }

    public static CustomUserDetails fromAdmin(Admin admin) {
        String roleName = switch (admin.getRole()) {
            case system_admin -> "system_admin_role";
            case restaurant_admin -> "restaurant_admin_role";
        };
        return new CustomUserDetails(
                admin.getEmail(),
                admin.getPasswordHash(),
                roleName,
                admin.getId().toString(),
                admin.getIsActive()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public String getRole() {
        return role;
    }

    public String getId() {
        return id;
    }
}