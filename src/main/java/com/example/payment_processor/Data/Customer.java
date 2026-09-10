package com.example.payment_processor.Data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
@Entity
@Getter
@Setter
public class Customer {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wallet wallet;

    public Customer(String firstName, String lastName, String email, String phoneNumber) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.wallet = new Wallet(Currency.getInstance("USD"), this, Instant.now());
    }

    public Customer() {
        this.id = UUID.randomUUID();
        this.wallet = new Wallet(Currency.getInstance("USD"), this, Instant.now());
    }
}
