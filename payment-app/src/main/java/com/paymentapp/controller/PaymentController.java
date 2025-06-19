package com.paymentapp.controller;

import com.paymentapp.dto.PaymentInitRequestDto;
import com.paymentapp.dto.PaymentProcessRequestDto;
import com.paymentapp.dto.PaymentResponseDto;
import com.paymentapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/init")
    public ResponseEntity<PaymentResponseDto> initPayment(
            @RequestBody PaymentInitRequestDto request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        return ResponseEntity.ok(paymentService.initPayment(request));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @PathVariable Long id,
            @RequestBody PaymentProcessRequestDto request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        return ResponseEntity.ok(paymentService.processPayment(id, request, userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        paymentService.cancelPayment(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        PaymentResponseDto payment = paymentService.getPayment(id, userId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }
} 