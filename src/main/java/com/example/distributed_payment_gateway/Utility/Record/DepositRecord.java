package com.example.distributed_payment_gateway.Utility.Record;

import com.example.distributed_payment_gateway.Utility.Enum.DepositStatus;

import java.time.Instant;
import java.util.UUID;

public final class DepositRecord {
    private DepositRecord() {
    }

    public record Response(UUID id, DepositStatus status, Instant scheduledFor) {
    }
}
