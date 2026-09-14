package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Payment;
import com.example.payment_processor.Data.Repository.BusinessRepository;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Data.Repository.PaymentRepository;
import com.example.payment_processor.Data.Repository.PaymentIdempotencyRepository;
import com.example.payment_processor.Data.Repository.WalletRepository;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Data.PaymentIdempotency;
import com.example.payment_processor.Utility.Enum.PaymentStatus;
import com.example.payment_processor.Utility.Exception.BalanceException;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final WalletRepository walletRepository;
    private final PaymentIdempotencyRepository paymentIdempotencyRepository;
    private final WebhookEventService webhookEventService;

    @Transactional
    public Payment createPayment(UUID customerId, UUID businessId, BigDecimal amount, String idempotencyKey)
            throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }
        if (businessId == null) {
            throw new IllegalActionException("Business id is required.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalActionException("Payment amount must be greater than zero.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalActionException("A non-empty idempotency key of at most 255 characters is required.");
        }
        idempotencyKey = idempotencyKey.trim();
        String requestHash = hashRequest(customerId, businessId, amount);

        PaymentIdempotency existing = paymentIdempotencyRepository.findByKeyForUpdate(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IllegalActionException("Idempotency key was already used with different payment data.");
            }
            if (existing.getPaymentId() == null) {
                throw new IllegalActionException("A payment with this idempotency key is currently being processed.");
            }
            return getPaymentById(existing.getPaymentId());
        }

        PaymentIdempotency idempotency = new PaymentIdempotency(idempotencyKey, requestHash, Instant.now());
        paymentIdempotencyRepository.saveAndFlush(idempotency);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalActionException("Customer not found for id: " + customerId));
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalActionException("Business not found for id: " + businessId));
        Wallet wallet = walletRepository.findByCustomer_IdForUpdate(customerId)
                .orElseThrow(() -> new IllegalActionException("Customer wallet not found for id: " + customerId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BalanceException("Insufficient funds to process payment.");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(Instant.now());

        Payment payment = new Payment(customerId, businessId, amount, Instant.now(), Instant.now());

        walletRepository.save(wallet);
        Payment savedPayment = paymentRepository.saveAndFlush(payment);
        webhookEventService.recordPaymentEvent(savedPayment, "payment.pending");
        idempotency.setPaymentId(savedPayment.getId());
        paymentIdempotencyRepository.save(idempotency);
        return savedPayment;
    }

    private String hashRequest(UUID customerId, UUID businessId, BigDecimal amount) throws IllegalActionException {
        try {
            String request = customerId + "|" + businessId + "|" + amount.stripTrailingZeros().toPlainString();
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(request.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hash.append(String.format("%02x", value));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalActionException("Unable to create payment idempotency fingerprint.");
        }
    }

    public Payment getPaymentById(Long paymentId) throws IllegalActionException {
        if (paymentId == null) {
            throw new IllegalActionException("Payment id is required.");
        }

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalActionException("Payment not found for id: " + paymentId));
    }

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) throws IllegalActionException {
        if (paymentId == null) {
            throw new IllegalActionException("Payment id is required.");
        }
        if (status == null) {
            throw new IllegalActionException("Payment status is required.");
        }

        Payment payment = getPaymentById(paymentId);
        payment.setPaymentStatus(status);
        return paymentRepository.save(payment);
    }
}
