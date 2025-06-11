package com.bookingapp.service;

import com.bookingapp.domain.Booking;
import com.bookingapp.event.BookingEventType;

public interface BookingEventService {
    void sendBookingEvent(Booking booking, BookingEventType eventType);
} 