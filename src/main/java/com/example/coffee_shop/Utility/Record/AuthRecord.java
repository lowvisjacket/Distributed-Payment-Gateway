package com.example.payment_processor.Utility.Record;

import jakarta.validation.constraints.NotNull;

public class AuthRecord {
    public record verifyEmail(
            @NotNull String email,
            @NotNull String code
    ) {}
}
