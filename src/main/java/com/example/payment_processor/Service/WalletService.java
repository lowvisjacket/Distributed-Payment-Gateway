package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Data.Repository.WalletRepository;
import com.example.payment_processor.Data.Repository.WalletIdempotencyRepository;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Data.WalletIdempotency;
import com.example.payment_processor.Utility.Exception.BalanceException;
import com.example.payment_processor.Utility.Exception.DisabledWallet;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final WalletIdempotencyRepository walletIdempotencyRepository;

    public Wallet updateWallet(Currency currency, String email) throws IllegalActionException {
        Wallet oldWallet = getWalletByEmail(email);
        return updateWallet(oldWallet, currency);
    }

    public Wallet updateWallet(Currency currency, UUID customerId) throws IllegalActionException {
        return updateWallet(getWalletByCustomerId(customerId), currency);
    }

    private Wallet updateWallet(Wallet oldWallet, Currency currency) throws IllegalActionException {
        validateCurrency(String.valueOf(currency));
        oldWallet.setCurrency(currency);
        walletRepository.save(oldWallet);
        return oldWallet;
    }

    @Transactional
    public Wallet createWalletViaCustomer(UUID customerId, Currency currency) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalActionException("Customer not found for id: " + customerId));

        if (walletRepository.findByCustomer_Id(customerId).isPresent()) {
            throw new IllegalActionException("Customer already has a wallet.");
        }

        Currency walletCurrency = currency != null ? currency : Currency.getInstance("USD");
        Wallet wallet = new Wallet(walletCurrency, customer);
        wallet.setUpdatedAt(Instant.now());
        wallet.setCustomer(customer);
        customer.setWallet(wallet);

        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet createWalletViaBusiness(Business business, Currency currency) throws IllegalActionException {
        if (business == null) {
            throw new IllegalActionException("Business id is required.");
        }

        Currency walletCurrency = currency != null ? currency : Currency.getInstance("USD");
        Wallet wallet = new Wallet(walletCurrency, business);
        wallet.setUpdatedAt(Instant.now());
        return walletRepository.save(wallet);
    }

    public Wallet getWalletByCustomerId(UUID customerId) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }

        return walletRepository.findByCustomer_Id(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
    }

    public Wallet getWalletByEmail(String email) throws IllegalActionException {
        if (email == null || email.isBlank()) {
            throw new IllegalActionException("Customer email is required.");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalActionException("Customer not found for email: " + email));
        isWalletDisabled(customer.getId());
        return getWalletByCustomerId(customer.getId());
    }

    @Transactional
    public Wallet deposit(UUID customerId, BigDecimal amount, String idempotencyKey)
            throws IllegalActionException {
        isWalletDisabled(customerId);
        validateCustomerId(customerId);
        validateAmount(amount);
        WalletIdempotency idempotency = startOperation(
                idempotencyKey,
                "DEPOSIT",
                customerId + "|" + amount.stripTrailingZeros().toPlainString()
        );
        Wallet completedWallet = getCompletedWallet(idempotency);
        if (completedWallet != null) {
            return completedWallet;
        }

        Wallet wallet = walletRepository.findByCustomer_IdForUpdate(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(Instant.now());
        Wallet savedWallet = walletRepository.save(wallet);
        completeOperation(idempotency, savedWallet.getId());
        return savedWallet;
    }

    @Transactional
    public Wallet withdraw(UUID customerId, BigDecimal amount, String idempotencyKey)
            throws IllegalActionException {
        isWalletDisabled(customerId);
        validateCustomerId(customerId);
        validateAmount(amount);
        WalletIdempotency idempotency = startOperation(
                idempotencyKey,
                "WITHDRAW",
                customerId + "|" + amount.stripTrailingZeros().toPlainString()
        );
        Wallet completedWallet = getCompletedWallet(idempotency);
        if (completedWallet != null) {
            return completedWallet;
        }

        Wallet wallet = walletRepository.findByCustomer_IdForUpdate(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BalanceException("Insufficient funds for withdrawal.");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(Instant.now());
        Wallet savedWallet = walletRepository.save(wallet);
        completeOperation(idempotency, savedWallet.getId());
        return savedWallet;
    }

    @Transactional
    public Wallet transfer(
            UUID fromCustomerId,
            UUID toCustomerId,
            BigDecimal amount,
            String idempotencyKey
    ) throws IllegalActionException {
        isWalletDisabled(fromCustomerId);
        isWalletDisabled(toCustomerId);
        validateAmount(amount);

        if (fromCustomerId == null || toCustomerId == null) {
            throw new IllegalActionException("Both customer ids are required for a transfer.");
        }
        if (fromCustomerId.equals(toCustomerId)) {
            throw new IllegalActionException("Cannot transfer to the same wallet.");
        }
        WalletIdempotency idempotency = startOperation(
                idempotencyKey,
                "TRANSFER",
                fromCustomerId + "|" + toCustomerId + "|" + amount.stripTrailingZeros().toPlainString()
        );
        Wallet completedWallet = getCompletedWallet(idempotency);
        if (completedWallet != null) {
            return completedWallet;
        }

        Wallet source = getWalletByCustomerId(fromCustomerId);
        Wallet target = getWalletByCustomerId(toCustomerId);
        UUID firstWalletId = source.getId().compareTo(target.getId()) < 0 ? source.getId() : target.getId();
        UUID secondWalletId = source.getId().compareTo(target.getId()) < 0 ? target.getId() : source.getId();
        Wallet firstWallet = walletRepository.findByIdForUpdate(firstWalletId)
                .orElseThrow(() -> new IllegalActionException("Source or target wallet no longer exists."));
        Wallet secondWallet = walletRepository.findByIdForUpdate(secondWalletId)
                .orElseThrow(() -> new IllegalActionException("Source or target wallet no longer exists."));
        Wallet sourceWallet = firstWallet.getId().equals(source.getId()) ? firstWallet : secondWallet;
        Wallet targetWallet = firstWallet.getId().equals(target.getId()) ? firstWallet : secondWallet;

        if (!sourceWallet.getCurrency().equals(targetWallet.getCurrency())) {
            throw new IllegalActionException("Transfers require wallets with the same currency.");
        }

        if (sourceWallet.getBalance().compareTo(amount) < 0) {
            throw new BalanceException("Insufficient funds for transfer.");
        }

        sourceWallet.setBalance(sourceWallet.getBalance().subtract(amount));
        targetWallet.setBalance(targetWallet.getBalance().add(amount));
        sourceWallet.setUpdatedAt(Instant.now());
        targetWallet.setUpdatedAt(Instant.now());

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);
        completeOperation(idempotency, sourceWallet.getId());
        return sourceWallet;
    }

    private WalletIdempotency startOperation(
            String idempotencyKey,
            String operation,
            String request
    ) throws IllegalActionException {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalActionException(
                    "A non-empty idempotency key of at most 255 characters is required."
            );
        }

        String normalizedKey = idempotencyKey.trim();
        String requestHash = hashRequest(operation, request);
        WalletIdempotency existing = walletIdempotencyRepository
                .findByKeyForUpdate(normalizedKey)
                .orElse(null);

        if (existing != null) {
            if (!existing.getOperation().equals(operation)
                    || !existing.getRequestHash().equals(requestHash)) {
                throw new IllegalActionException(
                        "Idempotency key was already used with different wallet operation data."
                );
            }
            if (existing.getResultWalletId() == null) {
                throw new IllegalActionException(
                        "A wallet operation with this idempotency key is currently being processed."
                );
            }
            return existing;
        }

        WalletIdempotency created = new WalletIdempotency(
                normalizedKey,
                operation,
                requestHash,
                Instant.now()
        );
        return walletIdempotencyRepository.saveAndFlush(created);
    }

    private void completeOperation(WalletIdempotency idempotency, UUID resultWalletId) {
        idempotency.setResultWalletId(resultWalletId);
        walletIdempotencyRepository.save(idempotency);
    }

    private Wallet getCompletedWallet(WalletIdempotency idempotency) throws IllegalActionException {
        if (idempotency.getResultWalletId() == null) {
            return null;
        }

        return walletRepository.findById(idempotency.getResultWalletId())
                .orElseThrow(() -> new IllegalActionException(
                        "Wallet operation completed, but its result wallet no longer exists."
                ));
    }

    private String hashRequest(String operation, String request) throws IllegalActionException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((operation + "|" + request).getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hash.append(String.format("%02x", value));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalActionException("Unable to create wallet idempotency fingerprint.");
        }
    }

    private void validateAmount(BigDecimal amount) throws IllegalActionException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalActionException("Amount must be greater than zero.");
        }
    }

    private void validateCustomerId(UUID customerId) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }
    }

    private void validateCurrency(String currency) throws IllegalActionException {
        if (currency == null || currency.isBlank() || Currency.getInstance(currency).getCurrencyCode() == null) {
            throw new IllegalActionException("Currency is required.");
        }
    }

    private void isWalletDisabled(UUID customerId) throws IllegalActionException {
        Wallet wallet = getWalletByCustomerId(customerId);
        if (wallet.isDisabled()) {
            throw new DisabledWallet("Wallet associated with this account has been disabled.");
        }
    }
}
