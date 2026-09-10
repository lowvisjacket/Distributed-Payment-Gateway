package com.example.payment_processor.Data;

import com.example.payment_processor.Utility.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private final UUID customerId;
    private final UUID businessId;
    private final BigDecimal amount;
    private final Instant paymentDate;
    private final Instant paymentTime;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public Payment(UUID customerId, UUID businessId, BigDecimal amount, Instant paymentDate, Instant paymentTime) {
        this.customerId = customerId;
        this.businessId = businessId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentTime = paymentTime;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public Payment() {
        this.id = null;
        this.customerId = null;
        this.businessId = null;
        this.amount = null;
        this.paymentDate = null;
        this.paymentTime = null;
    }
}
