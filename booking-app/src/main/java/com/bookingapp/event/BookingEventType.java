package com.bookingapp.event;

public enum BookingEventType {
    BOOKING_CREATED,      // When a new booking is created
    BOOKING_CONFIRMED,    // When payment is received
    BOOKING_COMPLETED,    // When rental period ends
    BOOKING_CANCELLED,    // When booking is cancelled by user
    BOOKING_EXPIRED,      // When payment time window expires
    PAYMENT_FAILED,       // When payment processing fails
    CAR_BOOKED,          // When car is booked
    CAR_RELEASED         // When car is released (cancelled or expired booking)
} 