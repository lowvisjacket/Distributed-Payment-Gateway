package com.example.payment_processor.Utility.Record;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class PaymentRecord {
    private PaymentRecord() {
    }

    /**
     * Request body for a payment made by the authenticated customer.
     * The paying customer is derived from the JWT principal.
     */
    public record CreateRequest(
            @NotNull UUID businessId,
            @NotNull
            @DecimalMin(value = "0.00", inclusive = false)
            @Digits(integer = 15, fraction = 2)
            BigDecimal amount
    ) {
    }
}
