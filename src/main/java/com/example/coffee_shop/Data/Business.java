package com.example.payment_processor.Data;

import com.example.payment_processor.Utility.Enum.BusinessCategory;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Entity
@Table(name = "business")
@Getter
@Setter
public class Business {
    @Id
    private final UUID businessId;
    private final String name;
    @Enumerated(EnumType.STRING)
    private final BusinessCategory businessCategory;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = true, unique = true)
    @JsonManagedReference("business-customer")
    private final Customer customer;

    @OneToOne
    @JoinColumn(name = "wallet_id", nullable = true, unique = true)
    @JsonManagedReference("business-wallet")
    private Wallet wallet;

    public Business(String name, BusinessCategory businessCategory, Customer customer) {
        this.customer = customer;
        this.businessId = UUID.randomUUID();
        this.name = name;
        this.businessCategory = businessCategory;
        this.wallet =  null;
    }

    public Business() {
        this.customer = null;
        this.businessId = UUID.randomUUID();
        this.name = "";
        this.businessCategory = null;
        this.wallet = null;
    }

}
