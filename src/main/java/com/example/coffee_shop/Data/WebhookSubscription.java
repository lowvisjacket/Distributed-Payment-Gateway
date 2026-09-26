package com.example.payment_processor.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_subscription")
@Getter
@NoArgsConstructor
public class WebhookSubscription {
    @Id
    private UUID id;
    private UUID businessId;
    private String url;
    private String secret;
    private boolean active;
    private Instant createdAt;

    public WebhookSubscription(UUID businessId, String url, String secret) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.url = url;
        this.secret = secret;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }
}
