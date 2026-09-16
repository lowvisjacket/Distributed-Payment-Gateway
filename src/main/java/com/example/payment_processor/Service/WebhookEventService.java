package com.example.payment_processor.Service;

import com.example.payment_processor.Data.Payment;
import com.example.payment_processor.Data.Repository.WebhookEventRepository;
import com.example.payment_processor.Data.Repository.WebhookSubscriptionRepository;
import com.example.payment_processor.Data.WebhookEvent;
import com.example.payment_processor.Data.WebhookSubscription;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service

public class WebhookEventService {
    private final WebhookEventRepository eventRepository;
    private final WebhookSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Autowired
    public WebhookEventService(WebhookEventRepository eventRepository, WebhookSubscriptionRepository subscriptionRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordPaymentEvent(Payment payment, String eventType) {
        eventRepository.save(new WebhookEvent(
                payment.getId(),
                payment.getBusinessId(),
                eventType
        ));
    }

    @Scheduled(fixedDelayString = "${webhook.dispatch-delay-ms:30000}")
    @Transactional
    public void dispatchPendingEvents() {
        Instant now = Instant.now();
        eventRepository.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        "PENDING",
                        now
                )
                .forEach(event -> dispatch(event, now));
    }

    private void dispatch(WebhookEvent event, Instant now) {
        WebhookSubscription subscription = subscriptionRepository
                .findByBusinessIdAndActiveTrue(event.getBusinessId())
                .orElse(null);
        if (subscription == null) {
            event.markRetry(now.plusSeconds(300), "No active webhook subscription.");
            eventRepository.save(event);
            return;
        }

        try {
            WebhookSubscriptionService.validateUrl(subscription.getUrl());
            String payload = objectMapper.writeValueAsString(Map.of(
                    "event", event.getEventType(),
                    "paymentId", event.getPaymentId(),
                    "businessId", event.getBusinessId(),
                    "createdAt", event.getCreatedAt()
            ));
            String signature = sign(payload, subscription.getSecret());

            restClient.post()
                    .uri(subscription.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Payment-Signature", signature)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            event.markDelivered(now);
        } catch (JsonProcessingException | RestClientException | IllegalActionException exception) {
            event.markRetry(
                    now.plusSeconds(retryDelaySeconds(event.getAttempts())),
                    exception.getMessage()
            );
        }
        eventRepository.save(event);
    }

    private String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign webhook payload.", exception);
        }
    }

    private long retryDelaySeconds(int attempts) {
        return Math.min(3600, 30L * (1L << Math.min(attempts, 6)));
    }
}
