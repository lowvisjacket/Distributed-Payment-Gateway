package com.example.payment_processor.Data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_event")
@Getter
@NoArgsConstructor
public class WebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long paymentId;
    private UUID businessId;
    private String eventType;
    private Instant createdAt;
    private Instant nextAttemptAt;
    private Instant deliveredAt;
    private int attempts;
    private String status;
    @Column(length = 1000)
    private String lastError;

    public WebhookEvent(Long paymentId, UUID businessId, String eventType) {
        this.paymentId = paymentId;
        this.businessId = businessId;
        this.eventType = eventType;
        this.createdAt = Instant.now();
        this.nextAttemptAt = this.createdAt;
        this.status = "PENDING";
        this.attempts = 0;
    }

    public void markDelivered(Instant deliveredAt) {
        this.status = "DELIVERED";
        this.deliveredAt = deliveredAt;
        this.lastError = null;
    }

    public void markRetry(Instant nextAttemptAt, String error) {
        this.attempts++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = error;
        if (this.attempts >= 5) {
            this.status = "FAILED";
        }
    }
}
