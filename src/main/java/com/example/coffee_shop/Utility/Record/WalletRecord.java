package com.example.payment_processor.Utility.Record;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletRecord {
    public record RequestCurrencyChange(
            @NotBlank
            @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a three-letter ISO code.")
            String currency
    ) {}

    /**
     * Request body for funding the authenticated customer's wallet.
     * The customer ID is deliberately omitted and must come from the JWT principal.
     */
    public record DepositFundsRequest(
            @NotNull UUID recipientCustomerId,
            @NotNull
            @DecimalMin(value = "0.00", inclusive = false)
            @Digits(integer = 15, fraction = 2)
            BigDecimal amount
    ) {}

    /**
     * Request body for a customer-to-customer wallet transfer.
     * The sending customer is intentionally absent: derive it from the authenticated JWT principal.
     */
    public record TransferFundsRequest(
            @NotNull UUID recipientCustomerId,
            @NotNull
            @DecimalMin(value = "0.00", inclusive = false)
            @Digits(integer = 15, fraction = 2)
            BigDecimal amount
    ) {}
}
