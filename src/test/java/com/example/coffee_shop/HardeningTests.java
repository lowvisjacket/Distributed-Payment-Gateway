package com.example.payment_processor;

import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Data.Repository.PaymentRepository;
import com.example.payment_processor.Data.Repository.WalletIdempotencyRepository;
import com.example.payment_processor.Data.Repository.WalletRepository;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Security.Jwt.JwtFilter;
import com.example.payment_processor.Security.Jwt.JwtService;
import com.example.payment_processor.Service.CustomerService;
import com.example.payment_processor.Service.WalletService;
import com.example.payment_processor.Service.WebhookSubscriptionService;
import com.example.payment_processor.Utility.Enum.PaymentStatus;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HardeningTests {

    @Test
    void currencyCannotChangeWhenWalletHasBalance() throws Exception {
        WalletRepository walletRepository = mock(WalletRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        Wallet wallet = new Wallet();
        wallet.setCurrency(Currency.getInstance("USD"));
        wallet.setBalance(new BigDecimal("1.00"));
        UUID customerId = UUID.randomUUID();
        when(walletRepository.findByCustomer_IdForUpdate(customerId)).thenReturn(Optional.of(wallet));

        WalletService service = walletService(walletRepository, paymentRepository);

        assertThrows(IllegalActionException.class,
                () -> service.updateWallet(Currency.getInstance("EUR"), customerId));
        verify(paymentRepository, never())
                .existsByCustomerIdAndPaymentStatus(customerId, PaymentStatus.PENDING);
    }

    @Test
    void currencyCanChangeForEmptyWalletWithoutPendingPayments() throws Exception {
        WalletRepository walletRepository = mock(WalletRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        Wallet wallet = new Wallet();
        wallet.setCurrency(Currency.getInstance("USD"));
        wallet.setBalance(BigDecimal.ZERO);
        UUID customerId = UUID.randomUUID();
        when(walletRepository.findByCustomer_IdForUpdate(customerId)).thenReturn(Optional.of(wallet));
        when(paymentRepository.existsByCustomerIdAndPaymentStatus(customerId, PaymentStatus.PENDING))
                .thenReturn(false);

        WalletService service = walletService(walletRepository, paymentRepository);
        service.updateWallet(Currency.getInstance("EUR"), customerId);

        assertEquals(Currency.getInstance("EUR"), wallet.getCurrency());
        verify(walletRepository).save(wallet);
    }

    @Test
    void webhookRejectsHttpAndPrivateNetworkAddresses() {
        assertThrows(IllegalActionException.class,
                () -> WebhookSubscriptionService.validateUrl("http://example.com/hook"));
        assertThrows(IllegalActionException.class,
                () -> WebhookSubscriptionService.validateUrl("https://127.0.0.1/hook"));
    }

    @Test
    void malformedJwtReturnsUnauthorizedWithoutCallingTheNextFilter() throws Exception {
        CustomerService customerService = mock(CustomerService.class);
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.extractCustomerId("bad-token"))
                .thenThrow(new MalformedJwtException("Malformed token"));
        JwtFilter filter = new JwtFilter(customerService, jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var filterChain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    private WalletService walletService(WalletRepository walletRepository, PaymentRepository paymentRepository) {
        return new WalletService(
                walletRepository,
                mock(CustomerRepository.class),
                mock(WalletIdempotencyRepository.class),
                paymentRepository
        );
    }
}
