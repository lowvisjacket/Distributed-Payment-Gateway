package com.example.payment_processor.Data;

import com.example.payment_processor.Utility.Enum.DepositStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ScheduledDeposit {
    @Id
    private UUID id;
    private String idempotencyKey;
    private UUID customerId;
    private BigDecimal amount;
    private Instant scheduledFor;
    private Instant createdAt;
    private Instant settledAt;
    @Enumerated(EnumType.STRING)
    private DepositStatus status;
    private String rejectionReason;

    public ScheduledDeposit(UUID id, String idempotencyKey, UUID customerId, BigDecimal amount, Instant scheduledFor) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.customerId = customerId;
        this.amount = amount;
        this.scheduledFor = scheduledFor;
        this.createdAt = Instant.now();
        this.status = DepositStatus.QUEUED;
    }
}
