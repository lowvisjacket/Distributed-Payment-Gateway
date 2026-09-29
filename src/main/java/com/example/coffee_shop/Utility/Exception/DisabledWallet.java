package com.example.payment_processor.Utility.Exception;

import org.springframework.security.authentication.DisabledException;

public class DisabledWallet extends DisabledException {
    public DisabledWallet(String message) {
        super(message);
    }
}
