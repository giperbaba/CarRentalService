package com.bookingapp.client;

import com.bookingapp.dto.payment.PaymentProcessRequestDto;
import com.bookingapp.dto.payment.PaymentProcessResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "payment-service",
    url = "${services.payment-service.url}"
)
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/process")
    PaymentProcessResponseDto processPayment(@RequestBody PaymentProcessRequestDto request);
} 