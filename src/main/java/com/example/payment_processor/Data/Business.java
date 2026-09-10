package com.example.payment_processor.Data;

import com.example.payment_processor.Utility.Enum.BusinessCategory;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;
@Entity
@Table(name = "business")
@Getter
public class Business {
    @Id
    private final UUID businessId;
    private final String name;
    @Enumerated(EnumType.STRING)
    private final BusinessCategory businessCategory;

    public Business(String name, BusinessCategory businessCategory) {
        this.businessId = UUID.randomUUID();
        this.name = name;
        this.businessCategory = businessCategory;
    }

    public Business() {
        this.businessId = UUID.randomUUID();
        this.name = "";
        this.businessCategory = null;
    }

}
