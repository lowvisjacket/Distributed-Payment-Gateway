package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Payment;
import com.example.payment_processor.Data.Repository.PaymentRepository;
import com.example.payment_processor.Data.Repository.WalletRepository;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Utility.Enum.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentSettlementService {
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WebhookEventService webhookEventService;

    @Scheduled(cron = "0 0 0 * * *", zone = "${payment.settlement-zone:UTC}")
    @Transactional
    public void settlePendingPayments() {
        paymentRepository.findIdsByPaymentStatus(PaymentStatus.PENDING)
                .forEach(this::settlePayment);
    }

    private void settlePayment(Long paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElse(null);
        if (payment == null || payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        Wallet businessWallet = walletRepository.findByBusiness_IdForUpdate(payment.getBusinessId())
                .orElse(null);

        if (businessWallet == null || businessWallet.isDisabled()) {
            refundSender(payment);
            payment.setPaymentStatus(PaymentStatus.ERROR);
            Payment savedPayment = paymentRepository.save(payment);
            webhookEventService.recordPaymentEvent(savedPayment, "payment.failed");
            return;
        }

        businessWallet.setBalance(businessWallet.getBalance().add(payment.getAmount()));
        businessWallet.setUpdatedAt(Instant.now());
        walletRepository.save(businessWallet);

        payment.setPaymentStatus(PaymentStatus.SUCCESSFUL);
        Payment savedPayment = paymentRepository.save(payment);
        webhookEventService.recordPaymentEvent(savedPayment, "payment.successful");
    }

    private void refundSender(Payment payment) {
        walletRepository.findByCustomer_IdForUpdate(payment.getCustomerId())
                .ifPresent(senderWallet -> {
                    senderWallet.setBalance(senderWallet.getBalance().add(payment.getAmount()));
                    senderWallet.setUpdatedAt(Instant.now());
                    walletRepository.save(senderWallet);
                });
    }
}
