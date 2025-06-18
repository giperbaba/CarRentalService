package com.bookingapp.service;

import com.bookingapp.domain.Booking;
import com.bookingapp.event.BookingEventType;
import com.bookingapp.event.CarEventType;

import java.util.UUID;

public interface BookingEventService {
    void sendBookingEvent(Booking booking, BookingEventType eventType);

    void sendCarEvent(Long carId, CarEventType eventType);
}