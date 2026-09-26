package com.example.payment_processor.Security.Email;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class VerificationCode {
    @Id
    private UUID id;
    private String email;
    private String code;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean deletionCode;

    public VerificationCode(String email, String code, Instant createdAt, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
