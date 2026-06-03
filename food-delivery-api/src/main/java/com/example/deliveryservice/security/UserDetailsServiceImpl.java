package com.example.deliveryservice.security;

import com.example.deliveryservice.entity.Admin;
import com.example.deliveryservice.entity.Courier;
import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.repository.AdminRepository;
import com.example.deliveryservice.repository.CourierRepository;
import com.example.deliveryservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService that loads user details from Customer, Courier, or Admin table.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email).orElse(null);
        if (customer != null) {
            return CustomUserDetails.fromCustomer(customer);
        }

        Courier courier = courierRepository.findByEmail(email).orElse(null);
        if (courier != null) {
            return CustomUserDetails.fromCourier(courier);
        }

        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            return CustomUserDetails.fromAdmin(admin);
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}