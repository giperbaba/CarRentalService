package com.bookingapp.integration;

import com.bookingapp.config.FeignClientInterceptor;
import com.bookingapp.dto.payment.PaymentProcessRequestDto;
import com.bookingapp.dto.payment.PaymentProcessResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "payment-service",
    url = "${integration.payment-service.url}",
    configuration = FeignClientInterceptor.class
)
public interface PaymentServiceClient {
    @PostMapping("/api/v1/payments/process")
    PaymentProcessResponseDto processPayment(@RequestBody PaymentProcessRequestDto request);
}