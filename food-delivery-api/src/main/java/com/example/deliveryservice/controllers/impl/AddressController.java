package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.AddAddressCommand;
import com.example.deliveryservice.dto.command.CreateAddressCommand;
import com.example.deliveryservice.dto.command.UpdateAddressCommand;
import com.example.deliveryservice.dto.response.CustomerAddressResponse;
import com.example.deliveryservice.services.AddressService;
import com.example.deliveryservice.services.CustomerAddressService; // Корректное имя сервиса
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService customerAddressService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAddressResponse add(@PathVariable UUID customerId,
                                       @RequestBody @Valid AddAddressCommand command) {
        return CustomerAddressResponse.fromEntity(
                customerAddressService.addAddress(customerId, command)
        );
    }

    @GetMapping
    public List<CustomerAddressResponse> getAll(@PathVariable UUID customerId) {
        return customerAddressService.getByCustomerId(customerId).stream()
                .map(CustomerAddressResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{addressId}")
    public CustomerAddressResponse getById(@PathVariable UUID customerId,
                                           @PathVariable UUID addressId) {
        return CustomerAddressResponse.fromEntity(customerAddressService.getById(addressId));
    }

    @PatchMapping("/{addressId}")
    public CustomerAddressResponse update(@PathVariable UUID customerId,
                                          @PathVariable UUID addressId,
                                          @RequestBody @Valid UpdateAddressCommand command) {
        return CustomerAddressResponse.fromEntity(
                customerAddressService.update(addressId, command)
        );
    }

    @PatchMapping("/{addressId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setDefault(@PathVariable UUID customerId,
                           @PathVariable UUID addressId) {
        customerAddressService.setDefault(customerId, addressId);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID customerId,
                       @PathVariable UUID addressId) {
        customerAddressService.delete(addressId);
    }
}