package com.example.distributed_payment_gateway.Data.Repository;

import com.example.distributed_payment_gateway.Data.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    List<WebhookEvent> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            String status,
            Instant now
    );
}
