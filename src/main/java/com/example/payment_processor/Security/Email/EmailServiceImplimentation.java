package com.example.payment_processor.Security.Email;

import com.example.payment_processor.Utility.Exception.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImplimentation implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public void sendVerificationEmail(Email email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(email.getRecipient());
        message.setSubject(email.getSubject());
        message.setText(email.getBody());
        try {
            javaMailSender.send(message);
        } catch (MailException ex) {
            ex.printStackTrace();
            throw new EmailException("Email could not be sent: " + ex.getMessage());
        }

    }

    @Override
    public void sendEmailWithAttachments(Email email) {

    }
}
