package com.paymentapp.mapper;

import com.paymentapp.dto.PaymentResponseDto;
import com.paymentapp.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    
    public PaymentResponseDto toDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
} 