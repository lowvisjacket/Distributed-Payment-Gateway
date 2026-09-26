package com.example.payment_processor.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "payment_idempotency")
@Getter
@Setter
@NoArgsConstructor
public class PaymentIdempotency {
    @Id
    private String idempotencyKey;
    private String requestHash;
    private Long paymentId;
    private Instant createdAt;

    public PaymentIdempotency(String idempotencyKey, String requestHash, Instant createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.createdAt = createdAt;
    }
}
