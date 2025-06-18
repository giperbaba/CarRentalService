package com.bookingapp.client;

import com.bookingapp.config.FeignConfig;
import com.bookingapp.dto.payment.PaymentInitRequestDto;
import com.bookingapp.dto.payment.PaymentProcessRequestDto;
import com.bookingapp.dto.payment.PaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "payment-service",
    url = "${services.payment-service.url}",
    configuration = FeignConfig.class
)
public interface PaymentServiceClient {
    
    @PostMapping("/api/payments/init")
    PaymentResponseDto initPayment(@RequestBody PaymentInitRequestDto request);

    @PostMapping("/api/payments/{id}/process")
    PaymentResponseDto processPayment(@PathVariable("id") Long id, @RequestBody PaymentProcessRequestDto request);

    @PostMapping("/api/payments/{id}/cancel")
    void cancelPayment(@PathVariable("id") Long id);

    @GetMapping("/api/payments/{id}")
    PaymentResponseDto getPayment(@PathVariable("id") Long id);

    @GetMapping("/api/payments/booking/{bookingId}")
    PaymentResponseDto getPaymentByBookingId(@PathVariable("bookingId") Long bookingId);
} 