package com.example.payment_processor.Security.Email;

import com.example.payment_processor.Utility.Exception.IllegalActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_BOUND = 1_000_000;
    private static final int CODE_LENGTH = 6;

    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;

    public void sendRegistrationCode(String email) {
        String code = generateCode();
        Instant createdAt = Instant.now();

        Email verificationEmail = new Email(
                email,
                "Your payment processor verification code",
                "Your verification code is: " + code
        );

        emailService.sendVerificationEmail(verificationEmail);

        verificationCodeRepository.save(new VerificationCode(
                email,
                code,
                createdAt,
                createdAt.plus(10, ChronoUnit.MINUTES)
        ));
    }

    private String generateCode() {
        return String.format("%0" + CODE_LENGTH + "d", RANDOM.nextInt(CODE_BOUND));
    }

    public boolean verifyCode(String code, String email) throws IllegalActionException {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        if (verificationCode == null) {
            return false;
        }
        if (Objects.equals(verificationCode.getCode(), code)) {
            verificationCodeRepository.delete(verificationCode);
            return true;
        }
        return false;
    }
}
