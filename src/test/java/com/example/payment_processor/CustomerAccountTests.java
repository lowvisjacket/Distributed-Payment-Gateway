package com.example.payment_processor;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Security.Email.Email;
import com.example.payment_processor.Security.Email.EmailService;
import com.example.payment_processor.Security.Email.VerificationCode;
import com.example.payment_processor.Security.Email.VerificationCodeRepository;
import com.example.payment_processor.Utility.Exception.EmailException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerAccountTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    VerificationCodeRepository verificationCodeRepository;

    @MockitoBean
    EmailService emailService;

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    void customerCanRegisterAndReceivesVerificationEmail() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(registerRequest(email))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(
                        "Customer registered. Check your email for the verification code."
                ));

        Customer customer = customerRepository.findByEmail(email).orElseThrow();
        assertFalse(customer.isAccountStatus());
        verify(emailService).sendVerificationEmail(any(Email.class));
        assertNotNull(verificationCodeRepository.findByEmail(email));
    }

    @Test
    void customerCannotLoginBeforeEmailVerification() throws Exception {
        String email = uniqueEmail();
        register(email);

        mockMvc.perform(loginRequest(email))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void customerCanVerifyEmailAndLogin() throws Exception {
        String email = uniqueEmail();
        register(email);

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        assertNotNull(verificationCode);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s"
                                }
                                """.formatted(email, verificationCode.getCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        assertTrue(customerRepository.findByEmail(email).orElseThrow().isAccountStatus());

        mockMvc.perform(loginRequest(email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void invalidVerificationCodeDoesNotUnlockCustomer() throws Exception {
        String email = uniqueEmail();
        register(email);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "000000"
                                }
                                """.formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertFalse(customerRepository.findByEmail(email).orElseThrow().isAccountStatus());
    }

    @Test
    void failedEmailDeliveryDoesNotCreateCustomer() throws Exception {
        String email = uniqueEmail();
        doThrow(new EmailException("Email could not be sent"))
                .when(emailService).sendVerificationEmail(any(Email.class));

        mockMvc.perform(registerRequest(email))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email could not be sent"));

        assertTrue(customerRepository.findByEmail(email).isEmpty());
    }

    @Test
    void unauthenticatedUserCannotAccessCustomerInfo() throws Exception {
        mockMvc.perform(get("/api/customer/"))
                .andExpect(status().isForbidden());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(registerRequest(email))
                .andExpect(status().isCreated());
        reset(emailService);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registerRequest(String email) {
        return post("/api/auth/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "firstName": "Test",
                          "lastName": "User",
                          "email": "%s",
                          "phoneNumber": "%s",
                          "password": "Test1234!"
                        }
                        """.formatted(email, uniquePhone()));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(String email) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "password": "Test1234!"
                        }
                        """.formatted(email));
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        return "555" + String.valueOf(Math.abs(UUID.randomUUID().getLeastSignificantBits()))
                .substring(0, 7);
    }
}
