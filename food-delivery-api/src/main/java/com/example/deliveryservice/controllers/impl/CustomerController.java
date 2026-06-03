package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.*;
import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Customer register(@RequestBody @Valid RegisterCustomerCommand command) {
        return customerService.register(command);
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable UUID id) {
        return customerService.getById(id);
    }

    @PatchMapping("/{id}")
    public Customer update(@PathVariable UUID id,
                           @RequestBody @Valid UpdateCustomerCommand command) {
        return customerService.update(id, command);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable UUID id,
                               @RequestBody @Valid ChangePasswordCommand command) {
        customerService.changePassword(id, command.oldPassword(), command.newPassword());
    }

    @PostMapping("/{id}/bonuses")
    public Customer addBonuses(@PathVariable UUID id,
                               @RequestParam int amount) {
        return customerService.addBonuses(id, amount);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        customerService.delete(id);
    }
}
