package com.bookingapp.dto.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentInitRequestDto {
    private Long bookingId;
    private BigDecimal amount;
    private UUID userId;
}