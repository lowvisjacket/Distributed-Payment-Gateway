package com.example.payment_processor.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_idempotency")
@Getter
@Setter
@NoArgsConstructor
public class WalletIdempotency {
    @Id
    private String idempotencyKey;
    private String operation;
    private String requestHash;
    private UUID resultWalletId;
    private Instant createdAt;

    public WalletIdempotency(
            String idempotencyKey,
            String operation,
            String requestHash,
            Instant createdAt
    ) {
        this.idempotencyKey = idempotencyKey;
        this.operation = operation;
        this.requestHash = requestHash;
        this.createdAt = createdAt;
    }
}
