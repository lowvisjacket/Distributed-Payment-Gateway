package com.example.payment_processor.Utility.Record;

import jakarta.validation.constraints.NotNull;

public class WalletRecord {
    public record RequestCurrencyChange(
            @NotNull String currency
    ) {}
}
