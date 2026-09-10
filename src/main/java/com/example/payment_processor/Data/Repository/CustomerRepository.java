package com.example.payment_processor.Data.Repository;

import com.example.payment_processor.Data.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

}
