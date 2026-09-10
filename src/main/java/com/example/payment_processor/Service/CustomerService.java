package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer createCustomer(String firstName, String lastName, String email, String phoneNumber) throws IllegalActionException {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalActionException("Customer first name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalActionException("Customer last name is required.");
        }
        if (email == null || email.isBlank() || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalActionException("A valid email address is required.");
        }
        if (phoneNumber == null || phoneNumber.isBlank() || !phoneNumber.matches("^[0-9+()\\-\\s]{7,20}$")) {
            throw new IllegalActionException("A valid phone number is required.");
        }

        Customer customer = new Customer(firstName.trim(), lastName.trim(), email.trim(), phoneNumber.trim());
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(UUID customerId) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }

        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalActionException("Customer not found for id: " + customerId));
    }
}

