package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.Payment;
import com.example.payment_processor.Utility.Enum.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
