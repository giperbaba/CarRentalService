package com.carapp.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCarNotFoundException(CarNotFoundException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(CarNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleCarNotAvailableException(CarNotAvailableException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(InvalidCarOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCarOperationException(InvalidCarOperationException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request parameters")
                .path(request.getDescription(false))
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        String message = "Invalid request format";
        
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException cause = (InvalidFormatException) ex.getCause();
            if (cause.getTargetType() != null && cause.getTargetType().isEnum()) {
                message = String.format("Invalid value for enum %s. Allowed values are: %s",
                        cause.getTargetType().getSimpleName(),
                        String.join(", ", getEnumValues(cause.getTargetType())));
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message(message)
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorResponse.ValidationError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Constraint violation")
                .path(request.getDescription(false))
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Access Denied: You don't have permission to access this resource")
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    private String[] getEnumValues(Class<?> enumClass) {
        return enumClass.getEnumConstants() != null
                ? java.util.Arrays.stream(enumClass.getEnumConstants())
                        .map(Object::toString)
                        .toArray(String[]::new)
                : new String[0];
    }
} 