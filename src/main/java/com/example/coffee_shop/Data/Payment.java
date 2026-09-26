package com.example.payment_processor.Data;

import com.example.payment_processor.Utility.Enum.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_customer")
    )
    @JsonIgnore
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_business")
    )
    @JsonIgnore
    private Business business;

    private final BigDecimal amount;
    private final Instant paymentDate;
    private final Instant paymentTime;
    private Instant timeMade;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public Payment(Customer customer, Business business, BigDecimal amount, Instant paymentDate, Instant paymentTime) {
        this.customer = customer;
        this.business = business;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentTime = paymentTime;
        this.timeMade = Instant.now();
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public Payment() {
        this.id = null;
        this.customer = null;
        this.business = null;
        this.amount = null;
        this.paymentDate = null;
        this.paymentTime = null;
        this.timeMade = null;
    }

    @JsonProperty("customerId")
    public UUID getCustomerId() {
        return customer == null ? null : customer.getId();
    }

    @JsonProperty("businessId")
    public UUID getBusinessId() {
        return business == null ? null : business.getBusinessId();
    }
}
