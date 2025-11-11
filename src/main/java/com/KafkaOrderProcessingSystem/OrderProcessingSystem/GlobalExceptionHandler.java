package com.KafkaOrderProcessingSystem.OrderProcessingSystem;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Global exception handler to manage validation errors
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation exceptions and return a structured error response
    @ExceptionHandler(MethodArgumentNotValidException.class)

    // Method to handle validation exceptions
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Map to hold field-specific error messages
        Map<String, String> errors = new HashMap<>();
        // Populate the map with field errors
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
