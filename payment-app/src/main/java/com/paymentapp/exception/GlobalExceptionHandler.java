package com.paymentapp.exception;

import com.paymentapp.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException ex) {
        log.error("Payment error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        
        // Check if the error message contains specific HTTP status codes
        if (ex.getMessage().contains("403")) {
            errorResponse.put("status", HttpStatus.FORBIDDEN.value());
            errorResponse.put("error", "Forbidden");
        } else if (ex.getMessage().contains("401")) {
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            errorResponse.put("error", "Unauthorized");
        } else if (ex.getMessage().contains("404")) {
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", "Not Found");
        } else {
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Bad Request");
        }
        
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity
                .status(errorResponse.get("status").equals(HttpStatus.FORBIDDEN.value()) ? 
                        HttpStatus.FORBIDDEN : 
                        errorResponse.get("status").equals(HttpStatus.UNAUTHORIZED.value()) ? 
                                HttpStatus.UNAUTHORIZED : 
                                errorResponse.get("status").equals(HttpStatus.NOT_FOUND.value()) ? 
                                        HttpStatus.NOT_FOUND : 
                                        HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred");
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
} 