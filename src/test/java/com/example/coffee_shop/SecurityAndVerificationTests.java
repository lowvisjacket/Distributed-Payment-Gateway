package com.example.payment_processor;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.REST.BusinessController;
import com.example.payment_processor.REST.CustomerController;
import com.example.payment_processor.REST.WalletController;
import com.example.payment_processor.REST.auth.AuthController;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Security.Email.Email;
import com.example.payment_processor.Security.Email.EmailService;
import com.example.payment_processor.Security.Email.VerificationCode;
import com.example.payment_processor.Security.Email.VerificationCodeRepository;
import com.example.payment_processor.Security.Email.VerificationCodeService;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Utility.Exception.EmailException;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityAndVerificationTests {
    private EmailService emailService;
    private VerificationCodeRepository verificationCodeRepository;
    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUpVerificationService() {
        emailService = mock(EmailService.class);
        verificationCodeRepository = mock(VerificationCodeRepository.class);
        verificationCodeService = new VerificationCodeService(emailService, verificationCodeRepository);
    }

    @Test
    void sendsSixDigitCodeAndSavesItAfterEmailSucceeds() {
        verificationCodeService.sendRegistrationCode("user@example.com");

        var emailCaptor = org.mockito.ArgumentCaptor.forClass(Email.class);
        var codeCaptor = org.mockito.ArgumentCaptor.forClass(VerificationCode.class);
        verify(emailService).sendVerificationEmail(emailCaptor.capture());
        verify(verificationCodeRepository).save(codeCaptor.capture());

        assertEquals("user@example.com", emailCaptor.getValue().getRecipient());
        assertTrue(codeCaptor.getValue().getCode().matches("\\d{6}"));
        assertEquals(emailCaptor.getValue().getBody(), "Your verification code is: "
                + codeCaptor.getValue().getCode());
        assertTrue(codeCaptor.getValue().getExpiresAt().isAfter(codeCaptor.getValue().getCreatedAt()));
    }

    @Test
    void doesNotSaveCodeWhenEmailSendingFails() {
        doThrow(new EmailException("Email could not be sent"))
                .when(emailService).sendVerificationEmail(any(Email.class));

        assertThrows(
                EmailException.class,
                () -> verificationCodeService.sendRegistrationCode("user@example.com")
        );
        verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
    }

    @Test
    void verifiesStoredCode() throws IllegalActionException {
        VerificationCode storedCode = new VerificationCode(
                "user@example.com",
                "123456",
                java.time.Instant.now(),
                java.time.Instant.now().plusSeconds(600)
        );
        when(verificationCodeRepository.findByEmail("user@example.com")).thenReturn(storedCode);

        assertTrue(verificationCodeService.verifyCode("123456", "user@example.com"));
        assertFalse(verificationCodeService.verifyCode("654321", "user@example.com"));
    }

    @Test
    void customerRegistrationSendsVerificationBeforeSavingCustomer() throws Exception {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        BCryptPasswordEncoder passwordEncoder = mock(BCryptPasswordEncoder.class);
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");

        CustomerService customerService = new CustomerService(
                customerRepository,
                passwordEncoder,
                verificationCodeService
        );

        Customer customer = new Customer(
                "Jane",
                "Doe",
                "jane@example.com",
                "5551234567",
                "Password1!"
        );
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.createCustomer(
                "Jane",
                "Doe",
                "jane@example.com",
                "5551234567",
                "Password1!"
        );

        verify(emailService).sendVerificationEmail(any(Email.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void customerIsNotSavedWhenRegistrationEmailFails() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        BCryptPasswordEncoder passwordEncoder = mock(BCryptPasswordEncoder.class);
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        doThrow(new EmailException("Email could not be sent"))
                .when(emailService).sendVerificationEmail(any(Email.class));

        CustomerService customerService = new CustomerService(
                customerRepository,
                passwordEncoder,
                verificationCodeService
        );

        assertThrows(
                EmailException.class,
                () -> customerService.createCustomer(
                        "Jane",
                        "Doe",
                        "jane@example.com",
                        "5551234567",
                        "Password1!"
                )
        );
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void successfulVerificationUnlocksAndSavesCustomer() throws Exception {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        BCryptPasswordEncoder passwordEncoder = mock(BCryptPasswordEncoder.class);
        CustomerService customerService = new CustomerService(
                customerRepository,
                passwordEncoder,
                verificationCodeService
        );
        Customer customer = new Customer(
                "Jane",
                "Doe",
                "jane@example.com",
                "5551234567",
                "encoded-password"
        );
        customer.setAccountStatus(false);
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(java.util.Optional.of(customer));

        customerService.verifyCustomerEmail("jane@example.com");

        assertTrue(customer.isAccountStatus());
        verify(customerRepository).save(customer);
    }

    @Test
    void authenticatedPrincipalCarriesCustomerUuid() {
        UUID customerId = UUID.randomUUID();
        User delegate = (User) User.withUsername("user@example.com")
                .password("password")
                .roles("CLIENT")
                .build();

        AuthenticatedCustomer principal = new AuthenticatedCustomer(customerId, delegate);

        assertEquals(customerId, principal.getCustomerId());
        assertEquals("user@example.com", principal.getUsername());
    }

    @Test
    void customerControllerUsesAuthenticatedCustomerId() throws Exception {
        CustomerService customerService = mock(CustomerService.class);
        CustomerController controller = new CustomerController();
        ReflectionTestUtils.setField(controller, "customerService", customerService);

        UUID customerId = UUID.randomUUID();
        AuthenticatedCustomer principal = authenticatedCustomer(customerId);
        Customer customer = new Customer(
                "Jane", "Doe", "jane@example.com", "5551234567", "encoded"
        );
        when(customerService.getCustomerById(customerId)).thenReturn(customer);

        ResponseEntity<?> response = controller.getCustomerInfo(principal);

        assertNotNull(response.getBody());
        verify(customerService).getCustomerById(customerId);
        verify(customerService, never()).getCustomerByEmail(anyString());
    }

    @Test
    void walletControllerUsesAuthenticatedCustomerId() throws Exception {
        com.example.payment_processor.Service.WalletService walletService =
                mock(com.example.payment_processor.Service.WalletService.class);
        com.example.payment_processor.Service.DepositService depositService = mock(com.example.payment_processor.Service.DepositService.class);
        WalletController controller = new WalletController(walletService, depositService);
        UUID customerId = UUID.randomUUID();
        AuthenticatedCustomer principal = authenticatedCustomer(customerId);

        controller.getWallet(principal);

        verify(walletService).getWalletByCustomerId(customerId);
        verify(walletService, never()).getWalletByEmail(anyString());
    }

    @Test
    void businessControllerUsesAuthenticatedCustomerId() throws Exception {
        CustomerService customerService = mock(CustomerService.class);
        com.example.payment_processor.Service.BusinessService businessService =
                mock(com.example.payment_processor.Service.BusinessService.class);
        BusinessController controller = new BusinessController();
        ReflectionTestUtils.setField(controller, "customerService", customerService);
        ReflectionTestUtils.setField(controller, "businessService", businessService);

        UUID customerId = UUID.randomUUID();
        AuthenticatedCustomer principal = authenticatedCustomer(customerId);
        Customer customer = mock(Customer.class);
        com.example.payment_processor.Data.Business business =
                mock(com.example.payment_processor.Data.Business.class);
        when(customer.getBusiness()).thenReturn(business);
        when(customerService.getCustomerById(customerId)).thenReturn(customer);

        controller.getBusiness(principal);

        verify(customerService).getCustomerById(customerId);
        verify(customerService, never()).getCustomerByEmail(anyString());
    }

    @Test
    void protectedControllersRequireAuthentication() {
        assertEquals("isAuthenticated()", CustomerController.class
                .getAnnotation(PreAuthorize.class).value());
        assertEquals("isAuthenticated()", BusinessController.class
                .getAnnotation(PreAuthorize.class).value());
        assertEquals("isAuthenticated()", WalletController.class
                .getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void businessRegistrationRequiresAuthentication() throws Exception {
        var method = AuthController.class.getDeclaredMethod(
                "createBusiness",
                com.example.payment_processor.Data.Business.class,
                org.springframework.security.core.userdetails.UserDetails.class
        );

        assertEquals("isAuthenticated()", method.getAnnotation(PreAuthorize.class).value());
    }

    private AuthenticatedCustomer authenticatedCustomer(UUID customerId) {
        User delegate = (User) User.withUsername("user@example.com")
                .password("password")
                .roles("CLIENT")
                .build();
        return new AuthenticatedCustomer(customerId, delegate);
    }
}
