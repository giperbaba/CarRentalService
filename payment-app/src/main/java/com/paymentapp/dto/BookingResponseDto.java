package com.paymentapp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingResponseDto {
    private Long id;
    private UUID userId;
    private BigDecimal totalPrice;
    private String status;
} 