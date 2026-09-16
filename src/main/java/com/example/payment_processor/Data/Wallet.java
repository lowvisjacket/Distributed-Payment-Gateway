package com.example.payment_processor.Data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JoinColumn(name = "customer_id", nullable = true, unique = true)
    @JsonBackReference("customer-wallet")
    private Customer customer;

    private Instant createdAt;
    private Instant updatedAt;
    private boolean disabled;

    @OneToOne
    @JoinColumn(name = "business_id", nullable = true, unique = true)
    @JsonBackReference("business-wallet")
    private Business business;

    public Wallet(Currency currency, Customer customer) {
        this.id = UUID.randomUUID();
        this.currency = currency;
        this.customer = customer;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.balance = BigDecimal.ZERO;
        this.disabled = false;
    }

    public Wallet(Currency currency, Business business) {
        this.id = UUID.randomUUID();
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
        this.disabled = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.business = business;
        this.customer = null;
    }

    public Wallet() {
        this.id = UUID.randomUUID();
        this.currency = null;
        this.customer = null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.balance = BigDecimal.ZERO;
        this.disabled = false;
    }

    @JsonProperty("businessId")
    public UUID getBusinessId() {
        return !(business == null)? business.getBusinessId(): null;
    }

    @JsonProperty("customerId")
    public UUID getCustomerId() {
        return !(customer == null)? customer.getId(): null;
    }

}
