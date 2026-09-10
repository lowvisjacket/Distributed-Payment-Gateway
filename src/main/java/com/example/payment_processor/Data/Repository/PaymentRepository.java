package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
