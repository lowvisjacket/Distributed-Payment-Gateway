package com.example.distributed_payment_gateway.Data.Repository;

import com.example.distributed_payment_gateway.Data.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    public Optional<Business> findByCustomerId(UUID customerId);
}
