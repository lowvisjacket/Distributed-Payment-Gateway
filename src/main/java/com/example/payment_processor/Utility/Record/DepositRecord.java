package com.example.payment_processor.Utility.Record;

import com.example.payment_processor.Utility.Enum.DepositStatus;

import java.time.Instant;
import java.util.UUID;

public final class DepositRecord {
    private DepositRecord() {
    }

    public record Response(UUID id, DepositStatus status, Instant scheduledFor) {
    }
}
