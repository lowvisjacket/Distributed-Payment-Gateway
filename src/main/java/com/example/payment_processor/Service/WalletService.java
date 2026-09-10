package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Data.Repository.CustomerRepository;
import com.example.payment_processor.Data.Repository.WalletRepository;
import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Utility.Exception.BalanceException;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;

    public Wallet createWallet(UUID customerId, Currency currency) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalActionException("Customer not found for id: " + customerId));

        if (walletRepository.findByCustomerId(customerId).isPresent()) {
            throw new IllegalActionException("Customer already has a wallet.");
        }

        Currency walletCurrency = currency != null ? currency : Currency.getInstance("USD");
        Wallet wallet = new Wallet(walletCurrency, customer, Instant.now());
        wallet.setUpdatedAt(Instant.now());
        wallet.setCustomer(customer);
        customer.setWallet(wallet);

        return walletRepository.save(wallet);
    }

    public Wallet getWalletByCustomerId(UUID customerId) throws IllegalActionException {
        if (customerId == null) {
            throw new IllegalActionException("Customer id is required.");
        }

        return walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
    }

    @Transactional
    public Wallet deposit(UUID customerId, BigDecimal amount) throws IllegalActionException {
        validateCustomerId(customerId);
        validateAmount(amount);

        Wallet wallet = walletRepository.findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(Instant.now());
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet withdraw(UUID customerId, BigDecimal amount) throws IllegalActionException {
        validateCustomerId(customerId);
        validateAmount(amount);

        Wallet wallet = walletRepository.findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new IllegalActionException("Wallet not found for customer id: " + customerId));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BalanceException("Insufficient funds for withdrawal.");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(Instant.now());
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet transfer(UUID fromCustomerId, UUID toCustomerId, BigDecimal amount) throws IllegalActionException {
        validateAmount(amount);

        if (fromCustomerId == null || toCustomerId == null) {
            throw new IllegalActionException("Both customer ids are required for a transfer.");
        }
        if (fromCustomerId.equals(toCustomerId)) {
            throw new IllegalActionException("Cannot transfer to the same wallet.");
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

        if (sourceWallet.getBalance().compareTo(amount) < 0) {
            throw new BalanceException("Insufficient funds for transfer.");
        }

        sourceWallet.setBalance(sourceWallet.getBalance().subtract(amount));
        targetWallet.setBalance(targetWallet.getBalance().add(amount));
        sourceWallet.setUpdatedAt(Instant.now());
        targetWallet.setUpdatedAt(Instant.now());

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);
        return sourceWallet;
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
}
