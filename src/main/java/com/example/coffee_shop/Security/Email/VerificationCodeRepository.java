package com.example.payment_processor.Security.Email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    VerificationCode findByEmail(String email);
    VerificationCode findByEmailAndDeletionCode(String email, boolean deletionCode);
}
