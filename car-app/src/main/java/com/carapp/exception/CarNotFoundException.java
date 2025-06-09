package com.carapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CarNotFoundException extends CarException {
    public CarNotFoundException(String message) {
        super(message);
    }

    public CarNotFoundException(Long id) {
        super(String.format("Car with id %d not found", id));
    }
} 