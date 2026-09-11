package com.example.payment_processor.Utility.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = IllegalActionException.class)
    public ResponseEntity<IllegalActionException> handleIllegalActionException(IllegalActionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex);
    }

//    @ExceptionHandler(value = Exception.class)
//    public ResponseEntity<Exception> handleException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
//    }

}
