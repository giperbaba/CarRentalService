package com.bookingapp.dto.payment;

import lombok.Data;

@Data
public class PaymentProcessResponseDto {
    private Long bookingId;
    private PaymentStatus status;
    private String transactionId;
    private Long paymentId;
} 