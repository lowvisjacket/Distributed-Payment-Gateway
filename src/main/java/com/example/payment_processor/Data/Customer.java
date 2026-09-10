package com.example.payment_processor.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;
@Entity
@Table(name = "customer")
@Getter
public class Customer {
    @Id
    private final UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public Customer(String firstName, String lastName, String email, String phoneNumber) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Customer() {
        this.id = UUID.randomUUID();
    }
}
