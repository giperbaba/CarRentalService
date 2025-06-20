package com.paymentapp.service;

import com.paymentapp.dto.PaymentResponseDto;
import com.paymentapp.dto.PaymentInitRequestDto;
import com.paymentapp.dto.PaymentProcessRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto initPayment(PaymentInitRequestDto request, String email);
    PaymentResponseDto processPayment(Long id, PaymentProcessRequestDto request, String userId, String email);
    PaymentResponseDto getPayment(Long id, String userId);
    Page<PaymentResponseDto> getAllPayments(Pageable pageable);
    PaymentResponseDto cancelPayment(Long id, String userId, String email);
} 