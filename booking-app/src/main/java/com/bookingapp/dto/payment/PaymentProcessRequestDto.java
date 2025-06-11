package com.bookingapp.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProcessRequestDto {
    private Long bookingId;
    private String paymentMethod;
    private Double amount;
} 