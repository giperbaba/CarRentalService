package com.common.event;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class BookingEvent {
    private Long bookingId;
    private UUID userId;
    private UUID carId;
    private com.common.enums.BookingStatus status;
} 