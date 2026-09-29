package com.example.distributed_payment_gateway.Security.Email;

public interface EmailService {

    void sendVerificationEmail(Email email);

    void sendEmailWithAttachments(Email email);
}
