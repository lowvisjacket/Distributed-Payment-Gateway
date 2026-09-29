package com.example.distributed_payment_gateway.Utility.Record;

import jakarta.validation.constraints.NotNull;

public class AuthRecord {
    public record verifyEmail(
            @NotNull String email,
            @NotNull String code
    ) {}
}
