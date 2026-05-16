package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.AddAddressCommand;
import com.example.deliveryservice.dto.command.UpdateAddressCommand;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerAddress addAddress(UUID customerId, AddAddressCommand command) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .region(command.region())
                .city(command.city())
                .street(command.street())
                .house(command.house())
                .apartment(command.apartment())
                .addressDetails(command.addressDetails())
                .postalCode(command.postalCode())
                .latitude(BigDecimal.valueOf(command.latitude()))
                .longitude(BigDecimal.valueOf(command.longitude()))
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

    public CustomerAddress update(UUID addressId, UpdateAddressCommand command) {
        CustomerAddress address = getById(addressId);

        if (command.city() != null) address.setCity(command.city());
        if (command.street() != null) address.setStreet(command.street());
        if (command.house() != null) address.setHouse(command.house());
        if (command.region() != null) address.setRegion(command.region());
        if (command.apartment() != null) address.setApartment(command.apartment());
        if (command.postalCode() != null) address.setPostalCode(command.postalCode());
        if (command.latitude() != null) address.setLatitude(BigDecimal.valueOf(command.latitude()));
        if (command.longitude() != null) address.setLongitude(BigDecimal.valueOf(command.longitude()));
        if (command.addressDetails() != null) address.setAddressDetails(command.addressDetails());

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
