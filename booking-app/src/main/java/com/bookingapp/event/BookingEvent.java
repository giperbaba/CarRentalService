package com.bookingapp.event;

import com.bookingapp.domain.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingEvent {
    private UUID eventId;
    private Long bookingId;
    private UUID userId;
    private UUID carId;
    private BookingEventType eventType;
    private BookingStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalPrice;
    private LocalDateTime eventTime;
} 