package com.example.payment_processor.Utility.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = IllegalActionException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalActionException(IllegalActionException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = DisabledWallet.class)
    public ResponseEntity<Map<String, Object>> handleDisabledWalletException(DisabledWallet ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.FORBIDDEN.value());
        map.put("message", ex.getMessage());
        return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledException(DisabledException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(value = BalanceException.class)
    public ResponseEntity<Map<String, Object>> handleBalanceException(BalanceException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(value = EmailException.class)
    public ResponseEntity<Map<String, Object>> handleEmailException(EmailException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

}
