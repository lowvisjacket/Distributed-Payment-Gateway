package com.example.payment_processor.Security.Email;

public interface EmailService {

    void sendVerificationEmail(Email email);

    void sendEmailWithAttachments(Email email);
}
