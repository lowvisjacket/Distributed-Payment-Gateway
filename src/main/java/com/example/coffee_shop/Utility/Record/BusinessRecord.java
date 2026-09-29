package com.example.payment_processor.Utility.Record;

import com.example.payment_processor.Data.Business;
import com.example.payment_processor.Utility.Enum.BusinessCategory;
import com.example.payment_processor.Utility.Enum.DepositStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BusinessRecord {
    public record Response(
            UUID businessId,
            String name,
            BusinessCategory businessCategory,
            UUID customerId,
            UUID walletId
    ) {
        public static Response from(Business business) {
            return new Response(
                    business.getBusinessId(),
                    business.getName(),
                    business.getBusinessCategory(),
                    business.getCustomer() == null ? null : business.getCustomer().getId(),
                    business.getWallet() == null ? null : business.getWallet().getId()
            );
        }
    }

    /**
     * Request body for a refund issued by the authenticated user's business.
     * Exactly one recipient identifier must be supplied.
     */
    public record RefundFundsRequest(
            UUID recipientCustomerId,
            UUID recipientBusinessId,
            @NotNull
            @DecimalMin(value = "0.00", inclusive = false)
            @Digits(integer = 15, fraction = 2)
            BigDecimal amount
    ) {
        @AssertTrue(message = "Provide exactly one recipient: a customer or a business.")
        public boolean hasExactlyOneRecipient() {
            return (recipientCustomerId == null) != (recipientBusinessId == null);
        }
    }

    public static record ManualApprovalDeposit(
            UUID id,
            String idempotencyKey,
            UUID customerId,
            BigDecimal amount,
            Instant createdAt,
            DepositStatus status
    ) {
        public ManualApprovalDeposit(
                UUID id,
                String idempotencyKey,
                UUID customerId,
                BigDecimal amount,
                Instant createdAt
        ) {
            this(id, idempotencyKey, customerId, amount, createdAt, DepositStatus.PENDING_MANUAL_REVIEW);
        }
    }
}
