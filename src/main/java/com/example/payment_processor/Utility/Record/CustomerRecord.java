package com.example.payment_processor.Utility.Record;

import com.example.payment_processor.Data.Customer;
import com.example.payment_processor.Utility.Enum.CustomerRole;
import com.example.payment_processor.Utility.Enum.DepositStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CustomerRecord {
    public record Response(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            CustomerRole customerRole,
            UUID walletId
    ) {
        public static Response from(Customer customer) {
            return new Response(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getCustomerRole(),
                    customer.getWallet().getId()
            );
        }
    }

    public record UpdateCustomer (
            String email,
            String firstName,
            String lastName
    )
    {
        public static Response from(Customer customer) {
            return new Response(
                    customer.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getCustomerRole(),
                    customer.getWallet().getId()
            );
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
