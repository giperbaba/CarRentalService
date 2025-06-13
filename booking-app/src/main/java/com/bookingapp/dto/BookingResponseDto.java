package com.bookingapp.dto;

import com.bookingapp.domain.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingResponseDto {
    private Long id;
    private UUID userId;
    private UUID carId;
    private Long paymentId;
    private BookingStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime actualEndDate;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 