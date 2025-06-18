package com.bookingapp.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CarEvent {
    private UUID eventId;
    private Long carId;
    private CarEventType eventType;
    private LocalDateTime eventTime;
} 