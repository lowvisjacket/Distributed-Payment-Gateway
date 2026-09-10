package com.example.payment_processor.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Wallet {
    @Id
    private UUID id;
    private Currency currency;
    private BigDecimal balance;
    @Version
    private long version;
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;
    private Instant createdAt;
    private Instant updatedAt;

    public Wallet(Currency currency, Customer customer, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.currency = currency;
        this.customer = customer;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.balance = BigDecimal.ZERO;
    }

    public Wallet() {
        this.id = UUID.randomUUID();
        this.currency = null;
        this.customer = null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.balance = BigDecimal.ZERO;
    }
}
