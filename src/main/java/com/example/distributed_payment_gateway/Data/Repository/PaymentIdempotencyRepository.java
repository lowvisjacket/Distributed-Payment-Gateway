package com.example.distributed_payment_gateway.Data.Repository;

import com.example.distributed_payment_gateway.Data.PaymentIdempotency;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotency, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from PaymentIdempotency i where i.idempotencyKey = :key")
    Optional<PaymentIdempotency> findByKeyForUpdate(@Param("key") String key);
}
