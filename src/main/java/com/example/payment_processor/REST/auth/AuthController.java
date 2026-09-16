package com.example.payment_processor.REST.auth;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Security.Email.VerificationCodeService;
import com.example.payment_processor.Security.Jwt.LoginRequest;
import com.example.payment_processor.Security.Jwt.JwtService;
import com.example.payment_processor.Service.BusinessService;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.AuthRecord;
import com.example.payment_processor.Utility.Record.BusinessRecord;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomerService customerService;
    private final BusinessService businessService;
    private final VerificationCodeService verificationCodeService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, CustomerService customerService, BusinessService businessService, VerificationCodeService verificationCodeService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customerService = customerService;
        this.businessService = businessService;
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid AuthRecord.verifyEmail verifyEmail) throws IllegalActionException {
        if (verificationCodeService.verifyCode(verifyEmail.code(), verifyEmail.email())) {
            customerService.verifyCustomerEmail(verifyEmail.email());
            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Email verified successfully."
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Invalid or expired verification code."
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> requestToken(@RequestBody LoginRequest loginRequest) throws IllegalActionException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        Customer customer = customerService.getCustomerByEmail(authentication.getName());
        String token = jwtService.generateToken(customer.getId());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/customer/register")
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody Customer customer) throws IllegalActionException {
        customerService.createCustomer(customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhoneNumber(), customer.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", HttpStatus.CREATED.value(),
                "message", "Customer registered. Check your email for the verification code."
        ));
    }

    @PostMapping("/business/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessRecord.Response> createBusiness(@RequestBody Business business, @AuthenticationPrincipal UserDetails userDetails) throws IllegalActionException {
        Customer customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Business createdBusiness = businessService.createBusiness(
                business.getName(),
                business.getBusinessCategory(),
                customer
        );
        return ResponseEntity.ok(BusinessRecord.Response.from(createdBusiness));
    }

    @PostMapping("/customer/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@AuthenticationPrincipal AuthenticatedCustomer userDetails) throws IllegalActionException {
        Customer customer = customerService.getCustomerById(userDetails.getCustomerId());
        verificationCodeService.sendRegistrationCode(userDetails.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.OK.value());
        map.put("message", "An email was sent to " + customer.getEmail() + " to verify");
        return ResponseEntity.ok(map);
    }

    @PostMapping("/customer/delete/verify-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> verifyDeletionCode(@RequestBody @Valid AuthRecord.verifyEmail verifyEmail, @AuthenticationPrincipal UserDetails userDetails) throws IllegalActionException {
        Customer customer = customerService.getCustomerByEmail(userDetails.getUsername());
        if (!customer.getEmail().equalsIgnoreCase(verifyEmail.email().trim())) {
            throw new IllegalActionException("Deletion verification email must match the authenticated customer.");
        }
        if (verificationCodeService.verifyCode(verifyEmail.code(), verifyEmail.email())) {
            customerService.deleteCustomerById(customer.getId());
        } else {
            throw new IllegalActionException("Invalid or expired verification code.");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.OK.value());
        map.put("message", "User has been deleted successfully");
        return ResponseEntity.ok(map);
    }
}
