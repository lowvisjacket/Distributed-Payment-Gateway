package com.example.payment_processor.Utility.Exception;

import org.bouncycastle.util.IPAddress;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = IllegalActionException.class)
    public ResponseEntity<IllegalActionException> handleIllegalActionException(IllegalActionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex);
    }

    @ExceptionHandler(value = DisabledWallet.class)
    public ResponseEntity<Map<String, Object>> handleDisabledWalletException(DisabledWallet ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.FORBIDDEN.value());
        map.put("message", ex.getMessage());
        map.put("timestamp", Instant.now().toString());
        return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<IllegalArgumentException> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex);
    }

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<DisabledException> handleDisabledException(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex);
    }

    @ExceptionHandler(value = BalanceException.class)
    public ResponseEntity<BalanceException> handleBalanceException(BalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<NoResourceFoundException> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex);
    }

}
