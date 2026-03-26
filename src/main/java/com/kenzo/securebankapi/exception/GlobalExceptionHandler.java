package com.kenzo.securebankapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {

        // 🔥 Case 1 — Cross-tenant or access control
        if (ex.getMessage().contains("Access denied")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN) // 403
                    .body(ex.getMessage());
        }

        // 🔥 Case 2 — Resource not found (tenant-safe)
        if (ex.getMessage().contains("Account not found")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body("Account not found");
        }

        // 🔥 Case 3 — Everything else
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong");
    }
}
