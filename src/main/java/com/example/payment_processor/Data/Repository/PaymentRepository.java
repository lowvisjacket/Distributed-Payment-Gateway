package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.Payment;
import com.example.payment_processor.Utility.Enum.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("select p.id from Payment p where p.paymentStatus = :status")
    List<Long> findIdsByPaymentStatus(@Param("status") PaymentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    boolean existsByCustomer_IdAndPaymentStatus(UUID customerId, PaymentStatus paymentStatus);

    default boolean existsByCustomerIdAndPaymentStatus(UUID customerId, PaymentStatus paymentStatus) {
        return existsByCustomer_IdAndPaymentStatus(customerId, paymentStatus);
    }
}
