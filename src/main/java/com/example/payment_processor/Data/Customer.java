package com.example.payment_processor.Data;

import com.example.payment_processor.Utility.Enum.CustomerRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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
    private String password;
    @Enumerated(EnumType.STRING)
    private CustomerRole customerRole;
    private boolean accountStatus;
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("customer-wallet")
    private Wallet wallet;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("business-customer")
    private Business business;

    public Customer(String firstName, String lastName, String email, String phoneNumber, String password) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.wallet = new Wallet(Currency.getInstance("USD"), this);
        this.password = password;
        this.customerRole = CustomerRole.CLIENT;
        this.accountStatus = false;
        this.business = null;
    }

    public Customer() {
        this.id = UUID.randomUUID();
        this.wallet = new Wallet(Currency.getInstance("USD"), this);
        this.business = null;
        this.accountStatus = false;
    }
}
