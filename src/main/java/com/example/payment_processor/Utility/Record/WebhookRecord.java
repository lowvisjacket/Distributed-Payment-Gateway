package com.example.payment_processor.Utility.Record;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class WebhookRecord {
    public record RegistrationRequest(@NotBlank String url) {
    }

    public record RegistrationResponse(
            UUID id,
            UUID businessId,
            String url,
            String secret
    ) {
    }
}
