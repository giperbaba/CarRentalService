package com.paymentapp.service;

import com.paymentapp.dto.PaymentResponseDto;
import com.paymentapp.dto.PaymentInitRequestDto;
import com.paymentapp.dto.PaymentProcessRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentResponseDto initPayment(PaymentInitRequestDto request);
    PaymentResponseDto processPayment(Long id, PaymentProcessRequestDto request);
    PaymentResponseDto getPayment(Long id);
    Page<PaymentResponseDto> getAllPayments(Pageable pageable);
    PaymentResponseDto cancelPayment(Long id);
} 