package com.paymentapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    private Long bookingId;
    private BigDecimal amount;
    private String cardNumber;
} 