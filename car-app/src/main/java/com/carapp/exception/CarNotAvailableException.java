package com.carapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CarNotAvailableException extends CarException {
    public CarNotAvailableException(String message) {
        super(message);
    }

    public CarNotAvailableException(Long id) {
        super(String.format("Car with id %d is not available for rent", id));
    }
} 