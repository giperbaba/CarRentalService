package com.carapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    Long paymentId;
    Long bookingId;
    UUID carId;
    UUID userId;
    PaymentStatus status;
}
