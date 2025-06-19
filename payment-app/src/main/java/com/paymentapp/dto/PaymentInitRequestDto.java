package com.paymentapp.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentInitRequestDto {
    private Long bookingId;
    private BigDecimal amount;
    private UUID userId;
    private UUID carId;
}