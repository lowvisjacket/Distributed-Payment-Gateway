package com.example.payment_processor.Service;

import com.example.payment_processor.Data.WebhookSubscription;
import com.example.payment_processor.Data.Repository.WebhookSubscriptionRepository;
import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookSubscriptionService {
    private final WebhookSubscriptionRepository subscriptionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookSubscription register(UUID businessId, String url) throws IllegalActionException {
        if (businessId == null) {
            throw new IllegalActionException("Business id is required.");
        }
        validateUrl(url);

        subscriptionRepository.findByBusinessIdAndActiveTrue(businessId)
                .ifPresent(existing -> {
                    existing.deactivate();
                    subscriptionRepository.save(existing);
                });

        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        WebhookSubscription subscription = new WebhookSubscription(
                businessId,
                url.trim(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes)
        );
        return subscriptionRepository.save(subscription);
    }

    public static void validateUrl(String url) throws IllegalActionException {
        if (url == null || url.isBlank()) {
            throw new IllegalActionException("Webhook URL is required.");
        }
        try {
            URI parsed = URI.create(url.trim());
            if (!"https".equalsIgnoreCase(parsed.getScheme())) {
                throw new IllegalActionException("Webhook URL must use HTTPS.");
            }
            if (parsed.getHost() == null || parsed.getUserInfo() != null) {
                throw new IllegalActionException("Webhook URL must include a host.");
            }
            rejectPrivateAddress(parsed.getHost());
        } catch (IllegalArgumentException exception) {
            throw new IllegalActionException("Webhook URL is invalid.");
        }
    }

    private static void rejectPrivateAddress(String host) throws IllegalActionException {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalActionException("Webhook URL must not resolve to a private network address.");
                }
            }
        } catch (UnknownHostException exception) {
            throw new IllegalActionException("Webhook URL host could not be resolved.");
        }
    }
}
