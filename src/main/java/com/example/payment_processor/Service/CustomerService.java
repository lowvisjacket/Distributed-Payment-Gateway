package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Security.Email.VerificationCodeService;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements UserDetailsService {
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationCodeService verificationCodeService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + email));
        return toUserDetails(customer);
    }

    public UserDetails loadUserById(UUID customerId) throws UsernameNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + customerId));
        return toUserDetails(customer);
    }

    private UserDetails toUserDetails(Customer customer) {
        UserDetails userDetails = User.withUsername(customer.getEmail())
                .password(customer.getPassword())
                .roles(customer.getCustomerRole().name())
                .disabled(!customer.isAccountStatus())
                .build();
        return new AuthenticatedCustomer(customer.getId(), userDetails);
    }

    public Customer createCustomer(String firstName, String lastName, String email, String phoneNumber, String password) throws IllegalActionException {
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

        String normalizedEmail = email.trim();
        String normalizedPhoneNumber = phoneNumber.trim();
        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalActionException("A customer with this email already exists.");
        }
        if (customerRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new IllegalActionException("A customer with this phone number already exists.");
        }

        if (password == null || password.isBlank() || !password.matches("^(?=\\S{8,}$)(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$")) {
            throw new IllegalActionException("Password must be at least 8 characters and contain at least one uppercase letter, one number, and one special character. Spaces are not allowed.");
        }

        Customer customer = new Customer(firstName.trim(), lastName.trim(), normalizedEmail, normalizedPhoneNumber, bCryptPasswordEncoder.encode(password));
        verificationCodeService.sendRegistrationCode(customer.getEmail());
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(UUID customerId) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }

        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalActionException("Customer not found for id: " + customerId));
    }

    public Customer getCustomerByEmail(String email) throws IllegalActionException {
        if (email == null || email.isBlank()) {
            throw new IllegalActionException("Customer email is required.");
        }
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalActionException("Customer not found for email: " + email));
    }

    public void verifyCustomerEmail(String email) throws IllegalActionException {
        Customer customer = getCustomerByEmail(email);
        customer.setAccountStatus(true);
        customerRepository.save(customer);
    }
}
