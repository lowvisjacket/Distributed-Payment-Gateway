package com.example.payment_processor.REST;

import com.example.payment_processor.Data.Wallet;
import com.example.payment_processor.Service.WalletService;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.example.payment_processor.Utility.Record.WalletRecord;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Wallet> getWallet(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IllegalActionException {
        return ResponseEntity.ok(walletService.getWalletByEmail(userDetails.getUsername()));
    }

    @PatchMapping("/currency")
    public ResponseEntity<Wallet> updateWallet(@AuthenticationPrincipal UserDetails userDetails, @RequestBody WalletRecord.RequestCurrencyChange currency) throws IllegalActionException {
        return ResponseEntity.ok(walletService.updateWallet(Currency.getInstance(currency.currency()), userDetails.getUsername()));
    }
}
