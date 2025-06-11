package com.bookingapp.config;

import com.bookingapp.exception.CarServiceException;
import com.bookingapp.exception.PaymentServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error during Feign call: {} - Status: {}", methodKey, response.status());
        
        if (methodKey.contains("CarServiceClient")) {
            return new CarServiceException(
                String.format("Car service error: %s - %s", response.status(), response.reason())
            );
        }
        
        if (methodKey.contains("PaymentServiceClient")) {
            return new PaymentServiceException(
                String.format("Payment service error: %s - %s", response.status(), response.reason())
            );
        }

        return new Exception(
            String.format("External service error: %s - %s", response.status(), response.reason())
        );
    }
} 