package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Security.AuthenticatedCustomer;
import com.example.payment_processor.Service.WalletService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.WalletRecord;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("isAuthenticated()")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Wallet> getWallet(
            @AuthenticationPrincipal AuthenticatedCustomer userDetails
    ) throws IllegalActionException {
        return ResponseEntity.ok(walletService.getWalletByCustomerId(userDetails.getCustomerId()));
    }

    @PatchMapping("/currency")
    public ResponseEntity<Wallet> updateWallet(@AuthenticationPrincipal AuthenticatedCustomer userDetails, @RequestBody WalletRecord.RequestCurrencyChange currency) throws IllegalActionException {
        return ResponseEntity.ok(walletService.updateWallet(Currency.getInstance(currency.currency()), userDetails.getCustomerId()));
    }
}
