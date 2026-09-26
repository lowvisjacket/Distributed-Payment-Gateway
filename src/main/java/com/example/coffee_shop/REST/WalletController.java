package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Service.DepositService;
import com.example.payment_processor.Service.WalletService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.WalletRecord;
import com.example.payment_processor.Utility.Record.DepositRecord;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("isAuthenticated()")
public class WalletController {
    private final WalletService walletService;
    private final DepositService depositService;

    public WalletController(WalletService walletService, DepositService depositService) {
        this.walletService = walletService;
        this.depositService = depositService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Wallet> getWallet(
            @AuthenticationPrincipal AuthenticatedCustomer userDetails
    ) throws IllegalActionException {
        return ResponseEntity.ok(walletService.getWalletByCustomerId(userDetails.getCustomerId()));
    }

    @PatchMapping("/currency")
    public ResponseEntity<Wallet> updateWallet(
            @AuthenticationPrincipal AuthenticatedCustomer userDetails,
            @RequestBody @Valid WalletRecord.RequestCurrencyChange currency
    ) throws IllegalActionException {
        return ResponseEntity.ok(walletService.updateWallet(Currency.getInstance(currency.currency()), userDetails.getCustomerId()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Wallet> transferToCustomer(
            @AuthenticationPrincipal AuthenticatedCustomer customer,
            @RequestBody @Valid WalletRecord.TransferFundsRequest transferFundsRequest,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) throws IllegalActionException {
        return ResponseEntity.ok(walletService.transfer(customer.getCustomerId(), transferFundsRequest.recipientCustomerId(), transferFundsRequest.amount(), idempotencyKey));
    }

    @PutMapping("/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositRecord.Response> deposit(
            @RequestBody @Valid WalletRecord.DepositFundsRequest depositFundsRequest,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) throws IllegalActionException {
        return ResponseEntity.ok(depositService.queueDeposit(
                depositFundsRequest.recipientCustomerId(),
                depositFundsRequest.amount(),
                idempotencyKey
        ));
    }
}
