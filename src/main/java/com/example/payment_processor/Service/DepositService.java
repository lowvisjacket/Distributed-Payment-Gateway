package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Repository.ScheduledDepositRepository;
import com.example.payment_processor.Data.Repository.WalletRepository;
import com.example.payment_processor.Data.ScheduledDeposit;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Utility.Enum.DepositStatus;
import com.example.payment_processor.Utility.Exception.DisabledWallet;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.CustomerRecord;
import com.example.payment_processor.Utility.Record.DepositRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService {
    private static final BigDecimal MANUAL_APPROVAL_THRESHOLD = new BigDecimal("1000.00");
    private static final ZoneId PROCESSING_ZONE = ZoneId.of("America/Chicago");

    private final ScheduledDepositRepository scheduledDepositRepository;
    private final WalletRepository walletRepository;
    private final ObjectProvider<ManualApprovalDepositStore> manualApprovalDepositStoreProvider;

    @Transactional
    public DepositRecord.Response queueDeposit(UUID customerId, BigDecimal amount, String idempotencyKey)
            throws IllegalActionException {
        validateRequest(customerId, amount, idempotencyKey);
        Wallet wallet = walletRepository.findByCustomer_IdForUpdate(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
        if (wallet.isDisabled()) {
            throw new DisabledWallet("Wallet associated with this account has been disabled.");
        }

        if (amount.compareTo(MANUAL_APPROVAL_THRESHOLD) > 0) {
            return queueManualApproval(customerId, amount, idempotencyKey);
        }

        ScheduledDeposit existing = scheduledDepositRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            assertMatchingRequest(existing, customerId, amount);
            return response(existing);
        }

        ScheduledDeposit deposit = new ScheduledDeposit(
                UUID.randomUUID(), idempotencyKey, customerId, amount, nextMidnight()
        );
        scheduledDepositRepository.save(deposit);
        return response(deposit);
    }

    @Scheduled(cron = "${deposit.processing-cron:0 0 0 * * *}", zone = "${deposit.processing-zone:America/Chicago}")
    @Transactional
    public void settleQueuedDeposits() {
        Instant now = Instant.now();
        scheduledDepositRepository.findIdsReadyForSettlement(
                        List.of(DepositStatus.QUEUED, DepositStatus.APPROVED), now
                )
                .forEach(this::settleDeposit);

        ManualApprovalDepositStore approvalStore = manualApprovalDepositStoreProvider.getIfAvailable();
        if (approvalStore != null) {
            approvalStore.findApprovedUnprocessed().forEach(this::settleManualDeposit);
        }
    }

    private DepositRecord.Response queueManualApproval(UUID customerId, BigDecimal amount, String idempotencyKey)
            throws IllegalActionException {
        ManualApprovalDepositStore approvalStore = manualApprovalDepositStoreProvider.getIfAvailable();
        if (approvalStore == null) {
            throw new IllegalActionException("Manual approval database is not configured for deposits above 1000.00.");
        }
        CustomerRecord.ManualApprovalDeposit existing = approvalStore.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.customerId().equals(customerId) || existing.amount().compareTo(amount) != 0) {
                throw new IllegalActionException("Idempotency key was already used with different deposit data.");
            }
            return new DepositRecord.Response(existing.id(), existing.status(), null);
        }

        CustomerRecord.ManualApprovalDeposit deposit = new CustomerRecord.ManualApprovalDeposit(
                UUID.randomUUID(), idempotencyKey, customerId, amount, Instant.now()
        );
        approvalStore.queue(deposit);
        return new DepositRecord.Response(deposit.id(), DepositStatus.PENDING_MANUAL_REVIEW, null);
    }

    private void settleManualDeposit(CustomerRecord.ManualApprovalDeposit approval) {
        ScheduledDeposit deposit = scheduledDepositRepository.findById(approval.id()).orElse(null);
        if (deposit == null) {
            deposit = new ScheduledDeposit(
                    approval.id(), approval.idempotencyKey(), approval.customerId(), approval.amount(), Instant.now()
            );
            deposit.setStatus(DepositStatus.APPROVED);
            scheduledDepositRepository.save(deposit);
        }
        settleDeposit(approval.id());
        ScheduledDeposit settled = scheduledDepositRepository.findById(approval.id()).orElse(null);
        if (settled != null && settled.getStatus() == DepositStatus.SETTLED) {
            manualApprovalDepositStoreProvider.getObject().markProcessed(approval.id());
        }
    }

    private void settleDeposit(UUID depositId) {
        ScheduledDeposit deposit = scheduledDepositRepository.findByIdForUpdate(depositId).orElse(null);
        if (deposit == null || (deposit.getStatus() != DepositStatus.QUEUED && deposit.getStatus() != DepositStatus.APPROVED)) {
            return;
        }

        Wallet wallet = walletRepository.findByCustomer_IdForUpdate(deposit.getCustomerId()).orElse(null);
        if (wallet == null || wallet.isDisabled()) {
            deposit.setStatus(DepositStatus.REJECTED);
            deposit.setRejectionReason("Recipient wallet is missing or disabled.");
            scheduledDepositRepository.save(deposit);
            return;
        }

        wallet.setBalance(wallet.getBalance().add(deposit.getAmount()));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        deposit.setStatus(DepositStatus.SETTLED);
        deposit.setSettledAt(Instant.now());
        scheduledDepositRepository.save(deposit);
    }

    private void validateRequest(UUID customerId, BigDecimal amount, String idempotencyKey) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Recipient customer id is required.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2) {
            throw new IllegalActionException("Deposit amount must be positive and have at most two decimal places.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalActionException("A non-empty idempotency key of at most 255 characters is required.");
        }
    }

    private void assertMatchingRequest(ScheduledDeposit deposit, UUID customerId, BigDecimal amount)
            throws IllegalActionException {
        if (!deposit.getCustomerId().equals(customerId) || deposit.getAmount().compareTo(amount) != 0) {
            throw new IllegalActionException("Idempotency key was already used with different deposit data.");
        }
    }

    private DepositRecord.Response response(ScheduledDeposit deposit) {
        return new DepositRecord.Response(deposit.getId(), deposit.getStatus(), deposit.getScheduledFor());
    }

    private Instant nextMidnight() {
        ZonedDateTime tomorrow = ZonedDateTime.now(PROCESSING_ZONE).toLocalDate().plusDays(1)
                .atStartOfDay(PROCESSING_ZONE);
        return tomorrow.toInstant();
    }
}
