package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {
    Optional<WebhookSubscription> findByBusinessIdAndActiveTrue(UUID businessId);
}
