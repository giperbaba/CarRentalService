package com.carapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private Long paymentId;
    private Long bookingId;
    private UUID carId;
    private UUID userId;
    private PaymentStatus status;
}