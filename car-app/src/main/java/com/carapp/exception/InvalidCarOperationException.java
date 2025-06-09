package com.carapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCarOperationException extends CarException {
    public InvalidCarOperationException(String message) {
        super(message);
    }
} 