package com.example.deliveryservice.services;

import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.entity.CustomerAddress;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.exceptions.ResourceOwnershipException;
import com.example.deliveryservice.repository.AddressRepository;
import com.example.deliveryservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public CustomerAddress addAddress(UUID customerId, String region, String city, String street,
                                      String house, String apartment, String addressDetails,
                                      String postalCode, Double latitude, Double longitude) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .region(region)
                .city(city)
                .street(street)
                .house(house)
                .apartment(apartment)
                .addressDetails(addressDetails)
                .postalCode(postalCode)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .isDefault(false)
                .build();

        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddress> getByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        return addressRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerAddress getById(UUID addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
    }

    public CustomerAddress update(UUID addressId, String region, String city, String street,
                                  String house, String apartment, String addressDetails,
                                  String postalCode, Double latitude, Double longitude) {
        CustomerAddress address = getById(addressId);

        if (city != null) address.setCity(city);
        if (street != null) address.setStreet(street);
        if (house != null) address.setHouse(house);
        if (region != null) address.setRegion(region);
        if (apartment != null) address.setApartment(apartment);
        if (postalCode != null) address.setPostalCode(postalCode);
        if (latitude != null) address.setLatitude(BigDecimal.valueOf(latitude));
        if (longitude != null) address.setLongitude(BigDecimal.valueOf(longitude));
        if (addressDetails != null) address.setAddressDetails(addressDetails);

        return addressRepository.save(address);
    }

    public void setDefault(UUID customerId, UUID addressId) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new ResourceOwnershipException("Address does not belong to customer");
        }

        addressRepository.clearDefaultForCustomer(customerId);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    public void delete(UUID addressId) {
        CustomerAddress address = getById(addressId);

        if (address.getIsDefault()) {
            throw new ResourceOwnershipException("Cannot delete default address");
        }

        addressRepository.deleteById(addressId);
    }

}
