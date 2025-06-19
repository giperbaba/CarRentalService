package com.carapp.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BookingEvent {
    private Long bookingId;
    private UUID userId;
    private UUID carId;
    private BookingStatus status;
}
