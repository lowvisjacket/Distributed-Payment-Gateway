package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByCustomer_Id(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.customer.id = :customerId")
    Optional<Wallet> findByCustomer_IdForUpdate(@Param("customerId") UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :walletId")
    Optional<Wallet> findByIdForUpdate(@Param("walletId") UUID walletId);
}
