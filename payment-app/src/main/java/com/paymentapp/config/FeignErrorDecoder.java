package com.paymentapp.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
            case 401 -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            case 403 -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
            case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
            case 500 -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
            default -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
        };
    }
} 