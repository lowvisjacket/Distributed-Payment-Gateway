package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    public Optional<Business> findByCustomerId(UUID customerId);
}
