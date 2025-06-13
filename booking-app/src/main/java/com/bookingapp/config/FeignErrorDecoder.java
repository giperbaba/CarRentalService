package com.bookingapp.config;

import com.bookingapp.constant.BookingConstants;
import com.bookingapp.exception.BookingException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error during Feign call: {} - Status: {}", methodKey, response.status());
        
        return switch (response.status()) {
            case 403 -> new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
            case 404 -> new BookingException(BookingConstants.ErrorMessages.CAR_NOT_FOUND);
            case 500 -> new BookingException(BookingConstants.ErrorMessages.INTERNAL_SERVER_ERROR);
            default -> new BookingException(BookingConstants.ErrorMessages.UNEXPECTED_ERROR);
        };
    }
} 