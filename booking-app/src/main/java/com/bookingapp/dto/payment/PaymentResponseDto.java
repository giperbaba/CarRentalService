package com.bookingapp.dto.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String cardNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 