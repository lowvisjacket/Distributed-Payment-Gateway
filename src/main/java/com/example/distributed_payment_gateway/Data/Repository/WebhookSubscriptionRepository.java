package com.example.distributed_payment_gateway.Data.Repository;

import com.example.distributed_payment_gateway.Data.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, UUID> {
    Optional<WebhookSubscription> findByBusinessIdAndActiveTrue(UUID businessId);
}
