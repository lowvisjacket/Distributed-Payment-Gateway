package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.ScheduledDeposit;
import com.example.payment_processor.Utility.Enum.DepositStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduledDepositRepository extends JpaRepository<ScheduledDeposit, UUID> {
    Optional<ScheduledDeposit> findByIdempotencyKey(String idempotencyKey);

    @Query("select d.id from ScheduledDeposit d where d.status in :statuses and d.scheduledFor <= :now")
    List<UUID> findIdsReadyForSettlement(@Param("statuses") List<DepositStatus> statuses, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from ScheduledDeposit d where d.id = :id")
    Optional<ScheduledDeposit> findByIdForUpdate(@Param("id") UUID id);
}
